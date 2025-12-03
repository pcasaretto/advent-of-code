(ns solution
  (:require [clojure.string :as str]))

(defn parse-rotation [s]
  (let [dir (first s)
        dist (parse-long (subs s 1))]
    {:dir dir :dist dist}))

(defn parse-input [input]
  (->> (str/split-lines input)
       (mapcat #(str/split % #",\s*"))
       (map str/trim)
       (filter (complement str/blank?))
       (map parse-rotation)))

(defn rotate [pos {:keys [dir dist]}]
  (let [delta (if (= dir \L) (- dist) dist)]
    (mod (+ pos delta) 100)))

(defn solve-part1 [input]
  (let [rotations (parse-input input)]
    (->> rotations
         (reductions rotate 50)
         rest
         (filter zero?)
         count)))

(defn count-zeros-crossed
  "Count how many times we pass through 0 during a rotation."
  [pos {:keys [dir dist]}]
  (if (= dir \R)
    ;; Going right from pos by dist crosses 0 at positions 100, 200, etc.
    ;; Example: from 50, going 1000 right reaches "position 1050", crossing 0 ten times
    (quot (+ pos dist) 100)
    ;; Going left from pos, first 0 is at distance=pos (or 100 if pos=0)
    ;; Then another 0 every 100 steps after that
    (let [steps-to-first-zero (if (zero? pos) 100 pos)]
      (if (< dist steps-to-first-zero)
        0
        (inc (quot (- dist steps-to-first-zero) 100))))))

(defn solve-part2 [input]
  (let [rotations (parse-input input)]
    (first
      (reduce
        (fn [[cnt pos] rotation]
          [(+ cnt (count-zeros-crossed pos rotation))
           (rotate pos rotation)])
        [0 50]
        rotations))))

(let [input (slurp *in*)]
  (println "Part 1:" (solve-part1 input))
  (println "Part 2:" (solve-part2 input)))
