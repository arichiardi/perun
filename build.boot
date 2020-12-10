(set-env!
 :source-paths #{"test"}
 :resource-paths #{"src"}
 :dependencies '[[boot/core "2.8.3" :scope "provided"]
                 [adzerk/boot-test "1.2.0" :scope "test"]
                 [adzerk/bootlaces "0.2.0" :scope "test"]
                 [degree9/boot-semver "RELEASE" :scope "test"]])

(require 'io.perun)
(def pod-deps
  (->> (ns-interns 'io.perun)
       vals
       (filter #(:deps (meta %)))
       (map deref)
       (reduce concat)
       (map #(conj % :scope "test"))))

(set-env! :dependencies #(into % pod-deps))

(require '[adzerk.bootlaces :refer :all])
(require '[io.perun.core :refer [+version+]])
(require '[io.perun-test])
(require '[boot.test :refer [runtests test-report test-exit]])

(bootlaces! +version+)

(task-options!
 aot {:all true}
 push {:ensure-branch  "master"
       :ensure-clean   false
       :ensure-version +version+}
 pom {:project 'perun
      :version +version+
      :description "Static site generator build with Clojure and Boot"
      :url         "https://github.com/hashobject/perun"
      :scm         {:url "https://github.com/hashobject/perun"}
      :license     {"name" "Eclipse Public License"
                    "url"  "http://www.eclipse.org/legal/epl-v10.html"}})

;; (deftask release-tag
;;   "Build and deploy to clojars."
;;   []
;;   (comp
;;    (version)
;;    (target)
;;    (build-jar)
;;    (push-release)))

(deftask new-minor-snapshot
  "Bump Install the artifact to the local .m2 but always using a SNAPSHOT version.
  Note that this task does not modify version.properties."
  []
  (comp
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (build-jar)))

(deftask release-snapshot
  "Release snapshot"
  []
  (comp (build-jar) (push-snapshot)))

(deftask build
  "Build process"
  []
  (comp
    (pom)
    (jar)
    (install)))

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (build)))

(deftask test
  "Run tests"
  []
  (comp
    (runtests)
    (test-report)
    (test-exit)))
