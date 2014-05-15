# dempster

This is an example implementation of the dempster shafar theory in clojure. The
dempster shafer rule accumulates two different information vector and produces
the plausibility, belief and doubt of every accumulated value. The conflict is
calculated automatically.

This was a university project.

## Installation

Download from this repository.

## Usage

(every routine from the root directory of this repository)

For usage with leiningen:

    lein repl
    (-main)

Otherwise with your installed version of clojure

    cd dempster/src/dempster/
    java -jar clojure-*.jar versions 1.4, 1.5, 1.6 should work)
    (load=file "core.clj")
    (ns dempster.core)
    (-main)

To use the jar

    lein uberjar
    java -jar target/dempster*.jar

## Options

There are no options available.

## Examples

This creates two evidences, combines them and produces the belief, doubt and
plausibility.

    lein repl
    (def bm (createMeasures 4 [[1 1 0 0 0.2] [1 1 0 0 0.2]]))
    (def bm1 (createMeasures 4 [[1 0 0 1 0.4] [0 0 1 1 0.6]]))
    (def res (get AccumulatedMeasures bm bm1))
    (printBasicMeasure res)

    Basic Measure:
    m([1000]) =     0.211
    m([1100]) =     0.000
    m([0011]) =     0.474
    m([1001]) =     0.316
    m([1111]) =     0.000
    nil

    (overallValues res)

     # : Pl(x)  |  B(x)   | Z(x)
     0 : 0.000  |  0.000  | 1.000 
     1 : 0.526  |  0.211  | 0.474 
     2 : 0.000  |  0.000  | 1.000 
     3 : 0.474  |  0.000  | 0.526 
    nil

## Bugs

* Output in the generated jar via (lein uberjar) does not work for
  plausibility, belief and doubt

## Sources

http://en.wikipedia.org/wiki/Dempster%E2%80%93Shafer_theory

## License

Copyright © 2014 kreutziger, theicarus

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
