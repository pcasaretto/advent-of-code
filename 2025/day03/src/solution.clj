(ns solution
  (:require [clojure.string :as str]))

(defn parse-bank [line]
  (mapv #(Character/digit % 10) line))

(defn parse-input [input]
  (->> (str/split-lines input)
       (map str/trim)
       (filter (complement str/blank?))
       (map parse-bank)))

(defn pick-max-digits
  "Greedily pick k digits from bank to form the largest number."
  [bank k]
  (loop [bank bank, k k, result []]
    (if (zero? k)
      result
      (let [window (subvec bank 0 (inc (- (count bank) k)))
            max-val (apply max window)
            idx (count (take-while #(not= % max-val) window))]
        (recur (subvec bank (inc idx)) (dec k) (conj result max-val))))))

(defn digits->number [digits]
  (reduce (fn [acc d] (+ (* 10 acc) d)) 0 digits))

(defn max-joltage-k
  "Find max k-digit number from picking k digits in order."
  [k bank]
  (digits->number (pick-max-digits bank k)))

(def max-joltage (partial max-joltage-k 2))

(defn solve-part1 [input]
  (let [banks (parse-input input)]
    (->> banks
         (map max-joltage)
         (reduce +))))

(defn solve-part2 [input]
  (let [banks (parse-input input)]
    (->> banks
         (map (partial max-joltage-k 12))
         (reduce +))))

(defn -main []
  (let [input (slurp *in*)]
    (println "Part 1:" (solve-part1 input))
    (println "Part 2:" (solve-part2 input))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
