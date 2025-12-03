(ns solution
  (:require [clojure.string :as str]))

(defn repeated-pattern?
  "Check if s is made of pattern repeated (len s)/(len pattern) times."
  [s pattern]
  (let [pattern-len (count pattern)
        s-len (count s)]
    (and (zero? (mod s-len pattern-len))
         (every? #(= pattern (subs s % (+ % pattern-len)))
                 (range 0 s-len pattern-len)))))

(defn invalid-id-v1?
  "Check if n is an 'invalid' ID - a digit sequence repeated exactly twice.
   Examples: 55, 6464, 123123"
  [n]
  (let [s (str n)
        len (count s)]
    (and (even? len)
         (let [half (/ len 2)
               first-half (subs s 0 half)
               second-half (subs s half)]
           (and (= first-half second-half)
                (not= (first first-half) \0))))))

(defn invalid-id-v2?
  "Check if n is an 'invalid' ID - a digit sequence repeated at least twice.
   Examples: 55, 123123, 123123123, 1111111"
  [n]
  (let [s (str n)
        len (count s)]
    ;; Try all possible pattern lengths from 1 to len/2
    (some (fn [pattern-len]
            (when (zero? (mod len pattern-len))
              (let [pattern (subs s 0 pattern-len)]
                (and (not= (first pattern) \0)
                     (>= (/ len pattern-len) 2)
                     (repeated-pattern? s pattern)))))
          (range 1 (inc (/ len 2))))))

(defn parse-range [s]
  (let [[start end] (str/split s #"-")]
    [(parse-long start) (parse-long end)]))

(defn parse-input [input]
  (->> (str/split input #"[,\s]+")
       (map str/trim)
       (filter (complement str/blank?))
       (map parse-range)))

(defn invalid-ids-in-range
  "Generate all invalid IDs within [start, end] inclusive using given predicate."
  [pred [start end]]
  (filter pred (range start (inc end))))

(defn solve-part1 [input]
  (let [ranges (parse-input input)]
    (->> ranges
         (mapcat (partial invalid-ids-in-range invalid-id-v1?))
         (reduce + 0))))

(defn solve-part2 [input]
  (let [ranges (parse-input input)]
    (->> ranges
         (mapcat (partial invalid-ids-in-range invalid-id-v2?))
         (reduce + 0))))

(let [input (slurp *in*)]
  (println "Part 1:" (solve-part1 input))
  (println "Part 2:" (solve-part2 input)))
