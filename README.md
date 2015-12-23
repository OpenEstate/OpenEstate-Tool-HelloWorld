OpenEstate-Tool-HelloWorld 1.0-beta37
=====================================

*OpenEstate-Tool-HelloWorld* is an addon for the freeware real estate software
[*OpenEstate-ImmoTool*](http://openestate.org), that does not provide any
features for end users. Instead this addon

-   is a starting point for developers, who want to create custom addons for
    *OpenEstate-ImmoTool*.
-   provides a build environment, that can easyliy be adapted by developers for
    their custom addons.
-   illustrates how to extend main menu and sidebar.
-   illustrates how to specify custom extension points.
-   illustrates how to accomplish operations on a project database.
-   illustrates how to implement a basic workflow on database objects (create,
    update, remove).
-   illustrates how to implement permissions on a multi user project.
-   illustrates how to implement internationalization based on *GetText*.


Dependencies
------------

-   *Java* 6 or newer (*Oracle Java* or *OpenJDK*)
-   *OpenEstate-ImmoTool* 1.0-beta37 (including its dependencies)


Requirements for developers
---------------------------

-   *Java Development Kit* (JDK) 6 or newer
    -   *Oracle JDK* and *OpenJDK* are known to work
-   *Netbeans* 8 (recommended but not required)
    -   Other IDE's like *Eclipse* or *IntelliJ* should also work. But we can't
        provide support for these applications.
-   *Apache Maven* 3
    -   The latest *Netbeans* packages already contain *Apache Maven*. In
        general it is not neccessary to download / install *Apache Maven*
        separately.
-   *GetText*
    -   Almost all *Linux* distributions provide packages for *GetText*.
    -   *Windows* users may use the
        [binaries by *gnuwin32*](http://gnuwin32.sourceforge.net/packages/gettext.htm)
    -   *Mac OS X* users may install *GetText* via [*brew*](http://brew.sh/) or
        [*MacPorts*](http://www.macports.org/).


Changelog
---------

Take a look at [`CHANGELOG.md`](CHANGELOG.md) for the full changelog.


License
-------

This library is licensed under the terms of
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
Take a look at [`LICENSE.txt`](LICENSE.txt) for the license text.


Todo
----

-   add missing javadoc comments
-   add more comments & documentations for developers
-   maybe implement some more extensions to illustrate how to use available
    extension points


Further informations
--------------------

-   [*OpenEstate-Tool-HelloWorld* at GitHub](https://github.com/OpenEstate/OpenEstate-Tool-HelloWorld)
-   [API documentation for *OpenEstate-ImmoTool*](http://manual.openestate.org/OpenEstate-Tool/)
-   [OpenEstate Blog](http://openestate.org)
