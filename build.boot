(def +version+ "1.1")

(set-env!
 :source-paths #{"src"}
 :resource-paths #{"res"}
 :target-path "tmp"
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(task-options!
 pom {:project 'chulper
      :version +version+}
 jar {:manifest {"Foo" "bar"}})

(require '[adzerk/bootlaces :refer :all])
(bootlaces! +version+)
