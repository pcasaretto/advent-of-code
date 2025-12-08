(ns solution
  (:require [clojure.string :as str]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (mapv #(mapv parse-long (str/split % #",")))))

(defn distance-sq [[x1 y1 z1] [x2 y2 z2]]
  (+ (* (- x2 x1) (- x2 x1))
     (* (- y2 y1) (- y2 y1))
     (* (- z2 z1) (- z2 z1))))

(defn all-pairs [n]
  (for [i (range n)
        j (range (inc i) n)]
    [i j]))

;; Union-Find with path compression
(defn make-uf [n]
  {:parent (vec (range n))
   :size (vec (repeat n 1))})

(defn find-root [uf i]
  (loop [i i, path []]
    (let [p (get-in uf [:parent i])]
      (if (= p i)
        [uf i]
        (recur p (conj path i))))))

(defn union [uf i j]
  (let [[uf ri] (find-root uf i)
        [uf rj] (find-root uf j)]
    (if (= ri rj)
      [uf false]  ; already in same circuit
      (let [si (get-in uf [:size ri])
            sj (get-in uf [:size rj])
            [small large] (if (< si sj) [ri rj] [rj ri])]
        [(-> uf
             (assoc-in [:parent small] large)
             (update-in [:size large] + (get-in uf [:size small])))
         true]))))

(defn circuit-sizes [uf]
  (let [n (count (:parent uf))]
    (->> (range n)
         (map #(second (find-root uf %)))
         frequencies
         vals)))

(defn solve-part1 [input]
  (let [points (parse-input input)
        n (count points)
        ;; Sort pairs by distance
        pairs (->> (all-pairs n)
                   (map (fn [[i j]] {:i i :j j :dist (distance-sq (points i) (points j))}))
                   (sort-by :dist))
        ;; Connect first 1000 pairs
        uf (reduce (fn [uf {:keys [i j]}]
                     (first (union uf i j)))
                   (make-uf n)
                   (take 1000 pairs))
        ;; Get 3 largest circuit sizes
        sizes (sort > (circuit-sizes uf))]
    (apply * (take 3 sizes))))

(defn solve-part2 [input]
  (let [points (parse-input input)
        n (count points)
        ;; Sort pairs by distance
        pairs (->> (all-pairs n)
                   (map (fn [[i j]] {:i i :j j :dist (distance-sq (points i) (points j))}))
                   (sort-by :dist))]
    ;; Connect until all in one circuit (n-1 actual merges needed)
    (loop [uf (make-uf n)
           remaining pairs
           merges 0
           last-pair nil]
      (if (= merges (dec n))
        ;; All connected - return product of X coordinates
        (let [p1 (points (:i last-pair))
              p2 (points (:j last-pair))]
          (* (first p1) (first p2)))
        (let [{:keys [i j] :as pair} (first remaining)
              [new-uf merged?] (union uf i j)]
          (recur new-uf
                 (rest remaining)
                 (if merged? (inc merges) merges)
                 (if merged? pair last-pair)))))))

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
