(def +version+ "1.2.2")

(set-env!
 :source-paths #{"src"}
 :target-path "tmp"
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 [adzerk/boot-cljs "2.1.3" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])
(require '[adzerk.boot-cljs :refer [cljs]])


(task-options!
 pom {:project 'chulper
      :version +version+}
 jar {:manifest {"Foo" "bar"}})

(bootlaces! +version+)
