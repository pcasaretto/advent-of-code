(ns solution
  (:require [clojure.string :as str]))

(defn parse-range [line]
  (let [[start end] (str/split line #"-")]
    [(parse-long start) (parse-long end)]))

(defn parse-input [input]
  (let [[ranges-section ids-section] (str/split input #"\n\n")]
    {:ranges (->> (str/split-lines ranges-section)
                  (map parse-range))
     :ids (->> (str/split-lines ids-section)
               (map parse-long))}))

(defn in-range? [id [start end]]
  (<= start id end))

(defn fresh? [ranges id]
  (some #(in-range? id %) ranges))

(defn solve-part1 [input]
  (let [{:keys [ranges ids]} (parse-input input)]
    (count (filter #(fresh? ranges %) ids))))

(defn merge-ranges [ranges]
  (let [sorted (sort-by first ranges)]
    (reduce (fn [merged [start end]]
              (if (empty? merged)
                [[start end]]
                (let [[prev-start prev-end] (peek merged)]
                  (if (<= start (inc prev-end))
                    (conj (pop merged) [prev-start (max prev-end end)])
                    (conj merged [start end])))))
            []
            sorted)))

(defn range-size [[start end]]
  (inc (- end start)))

(defn solve-part2 [input]
  (let [{:keys [ranges]} (parse-input input)]
    (->> ranges
         merge-ranges
         (map range-size)
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
