(ns solution
  (:require [clojure.string :as str]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (map #(mapv parse-long (str/split % #",")))))

(defn rect-area [[x1 y1] [x2 y2]]
  ;; Area includes both endpoints (tiles, not gaps)
  (* (inc (abs (- x2 x1))) (inc (abs (- y2 y1)))))

(defn solve-part1 [input]
  (let [points (vec (parse-input input))
        n (count points)]
    (reduce max 0
            (eduction
              (map (fn [[i j]] (rect-area (points i) (points j))))
              (for [i (range n), j (range (inc i) n)] [i j])))))

(defn line-between
  "Generate all points on a horizontal or vertical line between two points (inclusive)."
  [[x1 y1] [x2 y2]]
  (cond
    (= x1 x2) (for [y (range (min y1 y2) (inc (max y1 y2)))] [x1 y])
    (= y1 y2) (for [x (range (min x1 x2) (inc (max x1 x2)))] [x y1])
    :else (throw (ex-info "Not a straight line" {:p1 [x1 y1] :p2 [x2 y2]}))))

(defn build-boundary
  "Build set of all boundary tiles (red + green edges)."
  [red-tiles]
  (let [pairs (map vector red-tiles (concat (rest red-tiles) [(first red-tiles)]))]
    (into #{} (mapcat (fn [[p1 p2]] (line-between p1 p2)) pairs))))

(defn neighbors [[x y]]
  [[x (dec y)] [x (inc y)] [(dec x) y] [(inc x) y]])

(defn flood-fill-exterior
  "Find all exterior tiles by flood filling from outside the bounding box."
  [boundary min-x max-x min-y max-y]
  (let [start [(dec min-x) (dec min-y)]]
    (loop [queue [start]
           visited #{start}]
      (if (empty? queue)
        visited
        (let [[x y :as pos] (first queue)
              nbrs (->> (neighbors pos)
                        (filter (fn [[nx ny]]
                                  (and (<= (- min-x 1) nx (+ max-x 1))
                                       (<= (- min-y 1) ny (+ max-y 1))
                                       (not (visited [nx ny]))
                                       (not (boundary [nx ny]))))))]
          (recur (into (subvec queue 1) nbrs)
                 (into visited nbrs)))))))

(defn build-valid-tiles
  "Build set of all valid tiles (red + green = boundary + interior)."
  [red-tiles]
  (let [boundary (build-boundary red-tiles)
        xs (map first red-tiles)
        ys (map second red-tiles)
        min-x (apply min xs) max-x (apply max xs)
        min-y (apply min ys) max-y (apply max ys)
        exterior (flood-fill-exterior boundary min-x max-x min-y max-y)]
    ;; Valid = everything in bounding box that's not exterior
    (into #{}
          (for [x (range min-x (inc max-x))
                y (range min-y (inc max-y))
                :when (not (exterior [x y]))]
            [x y]))))

(defn get-polygon-edges
  "Get all edges of the polygon as [[x1 y1] [x2 y2]] pairs."
  [red-tiles]
  (map vector red-tiles (concat (rest red-tiles) [(first red-tiles)])))

(defn extract-vertical-edges
  "Extract and preprocess vertical edges for ray casting.
   Returns vec of [x y-lo y-hi] sorted by x."
  [red-tiles]
  (->> (get-polygon-edges red-tiles)
       (filter (fn [[[x1 _] [x2 _]]] (= x1 x2)))
       (map (fn [[[x1 y1] [_ y2]]]
              [x1 (min y1 y2) (max y1 y2)]))
       (sort-by first)
       vec))

(defn count-ray-crossings
  "Count how many vertical edges a rightward ray from (px, py) crosses."
  [vertical-edges px py]
  (loop [edges vertical-edges
         count 0]
    (if (empty? edges)
      count
      (let [[ex ey-lo ey-hi] (first edges)]
        (cond
          (<= ex px) (recur (rest edges) count)  ; edge not to the right
          (< ey-lo py ey-hi) (recur (rest edges) (inc count))  ; ray crosses
          :else (recur (rest edges) count))))))

(defn point-in-polygon?
  "Check if point is inside polygon using ray casting."
  [vertical-edges px py]
  (odd? (count-ray-crossings vertical-edges px py)))

(defn build-compressed-grid
  "Build valid cells with prefix sums using coordinate compression."
  [red-tiles]
  (let [vertical-edges (extract-vertical-edges red-tiles)
        xs (vec (sort (distinct (map first red-tiles))))
        ys (vec (sort (distinct (map second red-tiles))))
        nx (dec (count xs))
        ny (dec (count ys))
        ;; Precompute cell centers
        x-centers (mapv #(/ (+ (xs %) (xs (inc %))) 2) (range nx))
        y-centers (mapv #(/ (+ (ys %) (ys (inc %))) 2) (range ny))
        ;; Build 2D grid of valid cells in parallel (one row at a time)
        valid-grid (vec (pmap (fn [xi]
                                (let [cx (x-centers xi)]
                                  (mapv #(if (point-in-polygon? vertical-edges cx (y-centers %)) 1 0)
                                        (range ny))))
                              (range nx)))
        ;; Build prefix sums iteratively
        prefix (reduce
                 (fn [p xi]
                   (reduce
                     (fn [p yi]
                       (let [val (if (or (zero? xi) (zero? yi))
                                   0
                                   (+ (get-in valid-grid [(dec xi) (dec yi)])
                                      (get-in p [(dec xi) yi] 0)
                                      (get-in p [xi (dec yi)] 0)
                                      (- (get-in p [(dec xi) (dec yi)] 0))))]
                         (assoc-in p [xi yi] val)))
                     p
                     (range (inc ny))))
                 (vec (repeat (inc nx) (vec (repeat (inc ny) 0))))
                 (range (inc nx)))]
    {:xs xs :ys ys :prefix prefix}))

(defn rectangle-valid-compressed?
  "Check if rectangle is valid using prefix sums. O(1) query."
  [{:keys [xs ys prefix]} [x1 y1] [x2 y2]]
  (let [min-x (min x1 x2) max-x (max x1 x2)
        min-y (min y1 y2) max-y (max y1 y2)
        xi1 (java.util.Collections/binarySearch xs min-x)
        xi2 (java.util.Collections/binarySearch xs max-x)
        yi1 (java.util.Collections/binarySearch ys min-y)
        yi2 (java.util.Collections/binarySearch ys max-y)]
    (when (and (>= xi1 0) (>= xi2 0) (>= yi1 0) (>= yi2 0))
      (let [;; Number of valid cells in rectangle
            valid-count (+ (get-in prefix [xi2 yi2])
                          (- (get-in prefix [xi1 yi2]))
                          (- (get-in prefix [xi2 yi1]))
                          (get-in prefix [xi1 yi1]))
            ;; Total cells in rectangle
            total-cells (* (- xi2 xi1) (- yi2 yi1))]
        (= valid-count total-cells)))))

(defn solve-part2 [input]
  (let [red-tiles (vec (parse-input input))
        grid (build-compressed-grid red-tiles)
        n (count red-tiles)]
    (reduce max 0
            (eduction
              (filter (fn [[i j]] (rectangle-valid-compressed? grid (red-tiles i) (red-tiles j))))
              (map (fn [[i j]] (rect-area (red-tiles i) (red-tiles j))))
              (for [i (range n), j (range (inc i) n)] [i j])))))

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
