(def +version+ "1.1.1")

(set-env!
 :source-paths #{"src"}
 :target-path "tmp"
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(task-options!
 pom {:project 'chulper
      :version +version+}
 jar {:manifest {"Foo" "bar"}})

(bootlaces! +version+)
