NorthernWind
================================

[![Build Status](https://drone.io/bitbucket.org/tidalwave/northernwind-src/status.png)](https://drone.io/bitbucket.org/tidalwave/northernwind-src/latest)

NorthernWind is a lightweight CMS that uses a plain filesystem as its storage. It provides versioning by means of
a distributed SCM such as Git or Mercurial.

Please see the project website for more information: http://northernwind.tidalwave.it

A few examples of website running on NorthernWind:

* http://stoppingdown.net
* http://northernwind.tidalwave.it
* http://tidalwave.it


Bootstrapping
-------------

In order to build the project, run from the command line:

```mvn -DskipTests```

The project can be opened and built by a recent version of the NetBeans, Eclipse or Idea IDEs.


Documentation
-------------

More information can be found on the [homepage](http://tidalwave.kenai.com/northernwind) of the project.


Where can I get the latest release?
-----------------------------------
You can download source and binaries from the [download page](https://bitbucket.org/tidalwave/northernwind-src/src).

Alternatively you can pull it from the central Maven repositories:

```xml
<dependency>
    <groupId>it.tidalwave.northernwind<groupId>
    <artifactId>northernwind</artifactId>
    <version>-- version --</version>
</dependency>
```


Contributing
------------

We accept pull requests via BitBucket or GitHub.

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
* [Project Issue Tracker (Jira)](http://services.tidalwave.it/jira/browse/NW)
* [Project Continuous Integration (hudson)](http://ci.tidalwave.it/ci/view/NorthernWind)