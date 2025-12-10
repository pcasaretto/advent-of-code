(ns solution
  (:require [clojure.string :as str]))

(defn parse-target [s]
  "Parse indicator light diagram like [.##.] into vector of 0/1"
  (let [inner (subs s 1 (dec (count s)))]
    (mapv #(if (= % \#) 1 0) inner)))

(defn parse-button [s]
  "Parse button schematic like (0,2,3) into set of indices"
  (let [inner (subs s 1 (dec (count s)))]
    (if (str/blank? inner)
      #{}
      (into #{} (map parse-long (str/split inner #","))))))

(defn parse-joltage [s]
  "Parse joltage requirements like {3,5,4,7} into vector of integers"
  (let [inner (subs s 1 (dec (count s)))]
    (mapv parse-long (str/split inner #","))))

(defn parse-machine [line]
  "Parse a machine line into {:target [...] :buttons [#{...} ...] :joltage [...]}"
  (let [target-match (re-find #"\[[.#]+\]" line)
        button-matches (re-seq #"\([0-9,]*\)" line)
        joltage-match (re-find #"\{[0-9,]+\}" line)]
    {:target (parse-target target-match)
     :buttons (mapv parse-button button-matches)
     :joltage (parse-joltage joltage-match)}))

(defn parse-input [input]
  (->> (str/split-lines input)
       (filter #(not (str/blank? %)))
       (map parse-machine)))

;; Build augmented matrix [M | target] for GF(2) system
;; M[row][col] = 1 if button col toggles light row
(defn build-matrix [{:keys [target buttons]}]
  (let [n-lights (count target)
        n-buttons (count buttons)]
    (vec (for [light (range n-lights)]
           (conj (mapv #(if (contains? (buttons %) light) 1 0)
                       (range n-buttons))
                 (target light))))))

(defn xor-rows [row1 row2]
  (mapv bit-xor row1 row2))

(defn gaussian-eliminate
  "Row reduce matrix to RREF over GF(2). Returns [rref pivot-cols]"
  [matrix]
  (let [n-rows (count matrix)
        n-cols (dec (count (first matrix)))] ; exclude augmented column
    (loop [m matrix
           pivot-row 0
           pivot-col 0
           pivot-cols []]
      (if (or (>= pivot-row n-rows) (>= pivot-col n-cols))
        [m pivot-cols]
        ;; Find a row with 1 in pivot-col at or below pivot-row
        (if-let [swap-row (first (filter #(= 1 (get-in m [% pivot-col]))
                                         (range pivot-row n-rows)))]
          ;; Swap rows if needed
          (let [m (if (= swap-row pivot-row)
                    m
                    (-> m
                        (assoc pivot-row (m swap-row))
                        (assoc swap-row (m pivot-row))))
                ;; Eliminate all other 1s in this column
                m (reduce (fn [m row]
                            (if (and (not= row pivot-row)
                                     (= 1 (get-in m [row pivot-col])))
                              (assoc m row (xor-rows (m row) (m pivot-row)))
                              m))
                          m
                          (range n-rows))]
            (recur m (inc pivot-row) (inc pivot-col) (conj pivot-cols pivot-col)))
          ;; No pivot in this column, move to next column
          (recur m pivot-row (inc pivot-col) pivot-cols))))))

(defn solve-from-rref
  "Given RREF matrix and pivot columns, find minimum weight solution"
  [rref pivot-cols n-buttons]
  (let [n-rows (count rref)
        free-cols (vec (remove (set pivot-cols) (range n-buttons)))
        n-free (count free-cols)
        ;; Map pivot column -> row that determines it
        pivot-to-row (into {} (map-indexed (fn [row col] [col row]) pivot-cols))]
    ;; Check for inconsistency: row of all zeros with 1 in augmented column
    (when (some (fn [row]
                  (and (every? zero? (butlast row))
                       (= 1 (last row))))
                rref)
      (throw (ex-info "No solution exists" {})))
    ;; Enumerate all 2^n-free combinations
    (let [solutions
          (for [free-mask (range (bit-shift-left 1 n-free))]
            ;; Set free variables according to mask
            (let [solution (reduce (fn [sol [idx col]]
                                     (assoc sol col (if (bit-test free-mask idx) 1 0)))
                                   (vec (repeat n-buttons 0))
                                   (map-indexed vector free-cols))
                  ;; Solve for pivot variables using back-substitution
                  solution (reduce (fn [sol pivot-col]
                                     (let [row (pivot-to-row pivot-col)
                                           ;; value = target XOR sum of (coef * var) for non-pivot vars
                                           rhs (last (rref row))
                                           other-sum (reduce bit-xor 0
                                                             (for [c (range n-buttons)
                                                                   :when (and (not= c pivot-col)
                                                                              (= 1 (get-in rref [row c])))]
                                                               (sol c)))]
                                       (assoc sol pivot-col (bit-xor rhs other-sum))))
                                   solution
                                   pivot-cols)]
              solution))]
      (apply min-key #(reduce + %) solutions))))

(defn solve-machine [{:keys [target buttons] :as machine}]
  "Find minimum button presses using Gaussian elimination over GF(2)"
  (let [matrix (build-matrix machine)
        n-buttons (count buttons)
        [rref pivot-cols] (gaussian-eliminate matrix)
        solution (solve-from-rref rref pivot-cols n-buttons)]
    (reduce + solution)))

(defn solve-part1 [input]
  (let [machines (parse-input input)]
    (reduce + (map solve-machine machines))))

;; Part 2: Z3 SMT solver for minimum button presses
(defn generate-smt [{:keys [buttons joltage]}]
  "Generate SMT-LIB2 format for z3"
  (let [n-buttons (count buttons)
        n-counters (count joltage)
        ;; Build reverse map: counter -> list of button indices that affect it
        counter-buttons (vec (for [c (range n-counters)]
                               (filterv #(contains? (buttons %) c) (range n-buttons))))]
    (str
      ;; Declare variables
      (str/join "\n" (for [i (range n-buttons)]
                       (format "(declare-const x%d Int)" i)))
      "\n"
      ;; Non-negative constraints
      (str/join "\n" (for [i (range n-buttons)]
                       (format "(assert (>= x%d 0))" i)))
      "\n"
      ;; Sum constraints for each counter
      (str/join "\n" (for [c (range n-counters)
                          :let [btns (counter-buttons c)
                                target (joltage c)]]
                       (if (empty? btns)
                         (format "(assert (= 0 %d))" target)
                         (format "(assert (= (+ %s) %d))"
                                 (str/join " " (map #(str "x" %) btns))
                                 target))))
      "\n"
      ;; Minimize total presses
      (format "(minimize (+ %s))\n"
              (str/join " " (map #(str "x" %) (range n-buttons))))
      "(check-sat)\n"
      "(get-objectives)\n")))

(defn solve-with-z3 [smt-input]
  "Run z3 and extract the minimum value"
  (let [proc (-> (ProcessBuilder. ["z3" "-in"])
                 (.redirectErrorStream true)
                 (.start))
        out-stream (.getOutputStream proc)
        in-stream (.getInputStream proc)]
    (.write out-stream (.getBytes smt-input))
    (.close out-stream)
    (let [output (slurp in-stream)
          _ (.waitFor proc)]
      ;; Z3 output format: ((+ x0 x1 ...) VALUE) - extract the trailing number
      (when-let [match (re-find #"\(\([^)]+\)\s+(\d+)\)" output)]
        (parse-long (second match))))))

(defn solve-machine-joltage [{:keys [buttons joltage] :as machine}]
  "Find minimum button presses using Z3"
  (let [smt (generate-smt machine)
        result (solve-with-z3 smt)]
    (or result 0)))

(defn solve-part2 [input]
  (let [machines (parse-input input)]
    (reduce + (map solve-machine-joltage machines))))

(defmacro timed [expr]
  `(let [start# (System/nanoTime)
         result# ~expr
         ms# (/ (- (System/nanoTime) start#) 1e6)]
     [result# ms#]))

(defn -main []
  (let [input (slurp *in*)
        [r1 t1] (timed (solve-part1 input))
        [r2 t2] (timed (solve-part2 input))]
    (println (format "Part 1: %d (%.2fms)" r1 t1))
    (println (format "Part 2: %d (%.2fms)" r2 t2))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
