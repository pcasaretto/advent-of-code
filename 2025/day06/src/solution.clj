(ns solution
  (:require [clojure.string :as str]))

(defn parse-cells
  "Parse a row into cells (column groups separated by spaces)."
  [line]
  (->> (partition-by #(= % \space) line)
       (map #(apply str %))
       (remove #(str/blank? %))))

(defn solve-part1 [input]
  (let [lines (str/split-lines input)
        op-line (last lines)
        num-lines (butlast lines)]
    ;; Build running totals: {:mult m :add a} per column
    (let [totals (reduce
                   (fn [acc line]
                     (map (fn [cell total]
                            (if-let [n (parse-long (str/trim cell))]
                              {:mult (* (:mult total 1) n)
                               :add (+ (:add total 0) n)}
                              total))
                          (parse-cells line)
                          (concat acc (repeat {:mult 1 :add 0}))))
                   []
                   num-lines)
          ;; Pick correct value based on operation
          ops (parse-cells op-line)]
      (->> (map (fn [total op]
                  (if (str/includes? op "*")
                    (:mult total)
                    (:add total)))
                totals ops)
           (reduce +)))))

(defn solve-part2 [input]
  (let [lines (str/split-lines input)
        max-len (apply max (map count lines))
        padded (map #(format (str "%-" max-len "s") %) lines)
        op-line (last padded)
        num-lines (butlast padded)
        ;; Transpose to get columns
        cols (apply map str padded)
        ;; Group by space-only columns
        groups (->> cols
                    (partition-by #(every? #{\space} %))
                    (remove #(every? (fn [col] (every? #{\space} col)) %)))]
    (->> groups
         (map (fn [group]
                ;; Each column in group is a number (top-to-bottom digits)
                (let [col-strs (map #(apply str (butlast %)) group)  ; remove op char
                      nums (map #(parse-long (str/replace % #"\s" "")) col-strs)
                      op-char (first (remove #{\space} (map last group)))
                      op (if (= op-char \*) * +)]
                  (reduce op nums))))
         (reduce +))))

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
