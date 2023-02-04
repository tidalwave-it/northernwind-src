![Maven Central](https://img.shields.io/maven-central/v/it.tidalwave.northernwind/northernwind.svg)
[![Build Status](https://img.shields.io/jenkins/s/http/services.tidalwave.it/ci/job/NorthernWind_Build_from_Scratch.svg)](http://services.tidalwave.it/ci/view/NorthernWind)
[![Test Status](https://img.shields.io/jenkins/t/http/services.tidalwave.it/ci/job/NorthernWind.svg)](http://services.tidalwave.it/ci/view/NorthernWind)
[![Coverage](https://img.shields.io/jenkins/c/http/services.tidalwave.it/ci/job/NorthernWind.svg)](http://services.tidalwave.it/ci/view/NorthernWind)

NorthernWind
================================

NorthernWind is a lightweight CMS that uses a plain filesystem as its storage. It provides versioning by means of
a distributed SCM such as Git or Mercurial.

Please see the project website for more information: http://northernwind.tidalwave.it

A few examples of websites running on NorthernWind:

* http://stoppingdown.net
* http://tidalwave.it

To learn more about the things of this project, please have a look at its site - http://tidalwave.it/projects/northernwind - where more documentation, javadoc
and build reports are provided.


Bootstrapping
-------------

In order to build the project, run from the command line:

```mvn -DskipTests```

The project can be opened and built by a recent version of the NetBeans, Eclipse or Idea IDEs.


Documentation
-------------

More information can be found on the [homepage](http://tidalwave.it/projects/northernwind) of the project.


Contributing
------------

We accept pull requests via Bitbucket or GitHub.

There are some guidelines which will make applying pull requests easier for us:

* No tabs! Please use spaces for indentation.
* Respect the code style.
* Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source
  ode should be reformatted create a separate PR for this change.
* Provide TestNG tests for your changes and make sure your changes don't break any existing tests by running
```mvn clean test```.

If you plan to contribute on a regular basis, please consider filing a contributor license agreement. Contact us for
 more information


License
-------

Code is released under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt).


Additional Resources
--------------------

* [Tidalwave Homepage](http://tidalwave.it)
