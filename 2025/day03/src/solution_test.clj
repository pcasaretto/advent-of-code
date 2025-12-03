(ns solution-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [clojure.string :as str]))

;; Load the solution functions (won't execute -main due to guard)
(load-file "day03/src/solution.clj")

(deftest test-parse-bank
  (is (= [9 8 7 6 5 4 3 2 1 1 1 1 1 1 1] (solution/parse-bank "987654321111111")))
  (is (= [8 1 1 1 1 1 1 1 1 1 1 1 1 1 9] (solution/parse-bank "811111111111119"))))

(deftest test-max-joltage-part1
  (testing "Part 1 examples - pick 2 digits for max 2-digit number"
    (is (= 98 (solution/max-joltage (solution/parse-bank "987654321111111"))))
    (is (= 89 (solution/max-joltage (solution/parse-bank "811111111111119"))))
    (is (= 78 (solution/max-joltage (solution/parse-bank "234234234234278"))))
    (is (= 92 (solution/max-joltage (solution/parse-bank "818181911112111"))))))

(deftest test-solve-part1
  (let [example-input "987654321111111
811111111111119
234234234234278
818181911112111"]
    (is (= 357 (solution/solve-part1 example-input)))))

(deftest test-pick-max-digits
  (testing "pick 12 digits greedily"
    (is (= [9 8 7 6 5 4 3 2 1 1 1 1]
           (solution/pick-max-digits (solution/parse-bank "987654321111111") 12)))
    (is (= [8 1 1 1 1 1 1 1 1 1 1 9]
           (solution/pick-max-digits (solution/parse-bank "811111111111119") 12)))
    (is (= [4 3 4 2 3 4 2 3 4 2 7 8]
           (solution/pick-max-digits (solution/parse-bank "234234234234278") 12)))
    (is (= [8 8 8 9 1 1 1 1 2 1 1 1]
           (solution/pick-max-digits (solution/parse-bank "818181911112111") 12)))))

(deftest test-max-joltage-part2
  (testing "Part 2 examples - pick 12 digits for max 12-digit number"
    (is (= 987654321111 (solution/max-joltage-k 12 (solution/parse-bank "987654321111111"))))
    (is (= 811111111119 (solution/max-joltage-k 12 (solution/parse-bank "811111111111119"))))
    (is (= 434234234278 (solution/max-joltage-k 12 (solution/parse-bank "234234234234278"))))
    (is (= 888911112111 (solution/max-joltage-k 12 (solution/parse-bank "818181911112111"))))))

(deftest test-solve-part2
  (let [example-input "987654321111111
811111111111119
234234234234278
818181911112111"]
    (is (= 3121910778619 (solution/solve-part2 example-input)))))

(deftest test-leftmost-selection
  (testing "should pick leftmost max to maximize remaining options"
    ;; [9 9 1] picking 2: should pick idx 0 then idx 1 -> 99
    (is (= [9 9] (solution/pick-max-digits [9 9 1] 2)))
    (is (= 99 (solution/max-joltage-k 2 [9 9 1])))

    ;; [1 9 9] picking 2: can pick idx 1 then idx 2 -> 99
    (is (= [9 9] (solution/pick-max-digits [1 9 9] 2)))
    (is (= 99 (solution/max-joltage-k 2 [1 9 9])))))

(run-tests)
