(set-env!
 :source-paths #{"src"}
 :resource-paths #{"res"}
 :target-path "tmp"
 :dependencies '[[org.clojure/clojure "1.8.0"]])

(task-options!
 pom {:project 'chulper
      :version "0.0.1"}
 jar {:manifest {"Foo" "bar"}})
