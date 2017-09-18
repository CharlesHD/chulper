(ns chulper.bimap
  "That's a bijection map implementation by Christophe Grand. BigO perfs are the same as hashmap, but slower by a constant factor.")

(defprotocol InversibleMap
  (inverse [m]))

(defn- rdissoc [d r v]
  (if-let [[_ k] (find r v)] (dissoc d k) d))

#?(:clj
   (deftype Bimap [^clojure.lang.IPersistentMap direct reverse]
     Object
     (hashCode [x]
       (.hashCode direct))
     (equals [x y]
       (.equals direct y))
     clojure.lang.IPersistentMap
     (without [this k]
       (Bimap.
        (dissoc direct k)
        (rdissoc reverse direct k)))
     (assocEx [this k v]
       (if (or (contains? direct k) (contains? reverse v))
         (throw (Exception. "Key or value already present"))
         (assoc this k v)))
     java.lang.Iterable
     (iterator [this]
       (.iterator direct))
     clojure.lang.Associative
     (assoc [this k v]
       (Bimap.
        (assoc (rdissoc direct reverse v) k v)
        (assoc (rdissoc reverse direct k) v k)))
     (containsKey [this k]
       (contains? direct k))
     (entryAt [this k]
       (find direct k))
     clojure.lang.IPersistentCollection
     (cons [this x]
       (if-let [[k v] (and (vector? x) x)]
         (assoc this k v)
         (reduce (fn [m [k v]] (assoc m k v)) this x)))
     (empty [this]
       (.empty direct))
     (equiv [this x]
       (.equiv direct x))
     clojure.lang.Seqable
     (seq [this]
       (seq direct))
     clojure.lang.ILookup
     (valAt [this k else]
       (direct k else))
     (valAt [this k]
       (direct k))
     clojure.lang.IFn
     (invoke [this k] (direct k))
     (invoke [this k else] (direct k else))
     clojure.lang.Counted
     (count [this]
       (count direct))
     InversibleMap
     (inverse [this]
       (Bimap. reverse direct)))
   :cljs
   (deftype Bimap [^clojure.lang.IPersistentMap direct reverse]
     Object
     (toString [x]
       (pr-str* direct))
     (equiv [x y]
       (-equiv direct y))
     (keys [coll]
       (es6-iterator (keys direct)))
     (entries [coll]
       (es6-entries-iterator (seq direct)))
     (values [coll]
       (es6-iterator (vals direct)))
     (has [coll k]
       (contains? direct k))
     (get [coll k not-found]
       (-lookup direct k not-found))
     (forEach [coll f]
       (doseq [[k v] direct]
         (f v k)))
     ICloneable
     (-clone [_] (Bimap. direct reverse))
     IHash
     (-hash [_] (-hash direct))
     IIterable
     (-iterator [_] (-iterator direct))
     IAssociative
     (-assoc [this k v]
       (Bimap.
        (-assoc (rdissoc direct reverse v) k v)
        (-assoc (rdissoc reverse direct k) v k)))
     (-contains-key? [this k]
       (-contains-key? direct k))
     IFind
     (-find [_ k] (-find direct k))
     IMap
     (-dissoc [_ k]
       (Bimap.
        (-dissoc direct k)
        (rdissoc reverse direct k)))
     ICollection
     (-conj [this x]
       (if-let [[k v] (and (vector? x) x)]
         (-assoc this k v)
         (reduce (fn [m [k v]] (-assoc m k v)) this x)))
     IEmptyableCollection
     (-empty [_] (Bimap. (-empty direct) (-empty reverse)))
     IEquiv
     (-equiv [_ other] (-equiv direct other))
     ISeqable
     (-seq [this] (-seq direct))
     ICounted
     (-count [coll] (-count direct))
     ILookup
     (-lookup [coll k]
       (-lookup direct k))
     (-lookup [coll k else]
       (-lookup direct k else))
     IFn
     (-invoke [this k] (-lookup direct k))
     (-invoke [this k else] (-lookup direct k else))
     IKVReduce
     (-kv-reduce [this f init] (-kv-reduce direct f init))
     InversibleMap
     (inverse [this]
       (Bimap. reverse direct))))

(defn bimap [& kvs]
  (reduce (fn [m [k v]] (assoc m k v)) (Bimap. {} {}) (partition 2 kvs)))
