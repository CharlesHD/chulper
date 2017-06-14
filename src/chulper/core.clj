(ns chulper.core
  (:require [clojure.string :as s]))

;; miscs

(defn key-fn
  "Transform a function by applying key to all args passed to f
  ((key-fn name str) :hello :world) => \"helloworld\""
  [key f]
  (fn [& args] (apply f (map key args))))

(defn real-pmap
  "Like pmap, but launches futures instead of using a bounded threadpool.
  Useful when your tasks might block on each other, and you don't want to
  deadlock by exhausting the default clojure worker threadpool halfway through
  the collection. For instance,
      (let [n 1000
            b (CyclicBarrier. n)]
        (pmap (fn [i] [i (.await b)]) (range n)))
  ... deadlocks, but replacing `pmap` with `real-pmap` works fine."
  [f coll]
  (->> coll
       (map (fn launcher [x] (future (f x))))
       doall
       (map deref)))

;; map utilities

(defn map-inverse
  "Exchange key and values in a map.
   Since a value can be bound to many keys in the original map
   values in the produced map are wrapped in a seq.

  Example : (map-inverse {:a 1 :b 2 :c 1}) => {1 [:a :c] 2 [:b]}"
  [m]
  (reduce-kv
   (fn [m k v] (update m v conj k))
   {}
   m))

(defn map-vals
  "Apply f to every val of a map.

  Exemple (map-vals f {:a 1 :b 2}) => {:a (f 1) :b (f 2)}"
  [f m]
  (reduce-kv (fn [m' k v] (assoc m' k (f v))) {} m))

(defn apply-vals
  "Call vals of a map with x as argument"
  [m x]
  (map-vals #(% x) m))

(defn map-keys
  "Apply f to every keys of a map.

  Exemple (map-vals name {:a 1 :b 2}) => {\"a\" 1 \"b\" 2}

  The case where f do not map uniquely keys is by replacement by default (assoc style) :
  (map-vals (constantly :c) {:a 1 :b 2}) => {:c 2}

  You can specify a combinator for saying how to deal with redundant keys :
  (map-vals (constantly :c) {:a 1 :b 2} conj) => {:c '(1 2)}
  "
  ([f m]
   (reduce-kv (fn [m' k v] (assoc m' (f k) v)) {} m))
  ([f m comb]
   (reduce-kv (fn [m' k v] (update m' (f k) comb v)) {} m)))

(defn map-leaves
  "Recursively go down map and apply f to every non map leaf"
  [f m]
  (reduce-kv (fn [n k v] (if (map? v)
                           (assoc n k (map-leaves f v))
                           (assoc n k (f v)))) {} m))

(defn map-keys-recursively
  "recursively go down map and apply f to every encountered keys"
  [f m]
  (reduce-kv (fn [n k v] (if (map? v)
                           (assoc n (f k) (map-keys-recursively f v))
                           (assoc n (f k) v))) {} m))

(defn- leaves?
  [m]
  (not (some map? (vals m))))

(defn merge-leaves-with
  "merge maps at leave level."
  [f & ms]
  (if (some leaves? ms)
    (apply merge-with f ms)
    (apply merge-with (partial merge-leaves-with f) ms)))


;; algorithms
(defn fixpoint
  "Call f iteratively until a fixpoint is reached (it means (f a) = a).
   stop-pred is here to stop earlier the iteration or prevent an infinite loop."
  [f x stop-pred]
  (let [r (f x)]
    (if (or (stop-pred r) (= r x))
      r
      (recur f r stop-pred))))

(defn k-means
  [dist average k entryset]
  (let [mean (fn [ms a] (apply min-key #(dist a %) ms))
        step (fn [[means res]]
               (let [newres (reduce #(assoc %1 %2 (mean means %2)) {} entryset)
                     groups (vals (group-by val newres))
                     newmeans (map (comp average #(map key %)) groups)]
                 [newmeans newres]))]
    (second
     (fixpoint
      step
      [(take k (shuffle entryset))
       (reduce #(assoc %1 %2 nil) {} entryset)]
      (constantly false)))))

;; arithmetics

(defn average
  "The average of a collection of numbers."
  [numbers]
  (if (empty? numbers) 0
      (float (/ (apply + numbers) (count numbers)))))

(defn quadratic-mean
  "The quadratic mean of a collection of numbers."
  [numbers]
  (if (empty? numbers) 0
      (Math/sqrt (apply + (map #(* % %) numbers)))))

(defn variance
  "Variance of a collection of numbers."
  [numbers]
  (let [avg (average numbers)
        ecarts (map (comp #(* % %) #(- avg %)) numbers)
        ]
    (average ecarts)))

(defn ecart-type
  "Standard deviation of a collection of numbers."
  [numbers]
  (Math/sqrt (variance numbers)))

(defn geometric-mean
  [numbers]
  (Math/pow (reduce *' numbers) (/ 1 (count numbers))))
