(ns ^:figwheel-load zanbai.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [zanbai.core]))

(deftest test-1
  (testing "First test (should pass)"
    (is (= 1 1))
    (is (not= 1 2))))

(deftest test-2
  (testing "Second test (should fail)"
    (is (= 1 2))
    (is (not= 1 1))))
