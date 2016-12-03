(ns ^:figwheel-always zanbai.test
  (:require [cljs.test :refer-macros [run-all-tests]]
            [zanbai.core-test]
            [zanbai.events-test]))

(enable-console-print!)

(defn ^:export run []
  (run-all-tests #"zanbai\..*-test"))
