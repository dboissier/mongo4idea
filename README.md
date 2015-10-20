# Mongo Plugin for IntelliJ IDEA version 0.7.2-SNAPSHOT

* Better mongodb 3.0 compatibility (MONGDB_CR or SCRAM_SHA1 authentication macanism can be set in the configuration)
* [Changelog](https://github.com/dboissier/mongo4idea/blob/master/CHANGELOG.txt)

* **Important note**: This release will be the last! As I announced in this [blog post](http://codinjutsu.blogspot.fr/2014/07/hi-all-mongo-plugin-seems-to-grow-in.html). I will work on **nosql4idea** plugin that will integrate Mongo and other Document Oriented Database such as CouchBase and Redis.

## Description
This plugin integrates MongoDB Servers with database/collections tree, Query Runner and Shell console.

## Plugin Compatibility
This plugin was built with JDK 1.6 and ideaIC-14.1 version (Idea 13 is not supported anymore).

## How to install it?

Download this plugin from your IDE (Mongo Plugin)

## Configuration steps

When you open the Mongo explorer, you will see a panel:

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithoutDB.png?raw=true)

* To manage your Mongo servers, click on the Mongo Settings button located on the upper toolbar of the Mongo explorer Right Panel

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-configuration.png?raw=true)

* You can edit your mongo shell path with a test button
* To add a server, click on the **[+]** button and a dialog will appear
* Set a label for your server, this will be more convenient to distinguish each of them in the Mongo explorer
* Set the server info as the example in the above screenshot
* If your access is restricted to a specific database, just type it in the corresponding field
* Put your credentials if your server requires authentication
* You can also specify if your connection uses SSL
* You can let the plugin connect to the server on the IDE startup by clicking on the corresponding checkbox
* If you want to hide some collections, you can put them in the **Collections to ignore** field.
* You can click on the **Test Connection** button to check your server configuration

## Usage

### Mongo Explorer

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithDB.png?raw=true)

For each server, the tree displays its databases with all of their collections.

* To view the content of your server, just double-click on it.
* To view the content of a collection, double-click on it and the results will be displayed in an editor tab, alongside your open files.
If you double-click on another collection, a new tab will be created for it.

If you want clear a collection or database, you can do so by right clicking on it and selecting **Drop collection/database**. Be cautious with this operation, it should not be used in a production environment.

### Mongo collection tab view

The panel shows all documents of the collections (max 300 records by default).

![Collection view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-collectionTabView.png?raw=true)

You can copy the result and paste it in a text editor.

#### \[NEW\] Querying

If you want to run a *find* query, click on the Magnifying glass icon or use the **CTRL+F** shortcut.

The query panel will appear at the top.

Type your filter, projection or sort query fragment in JSON format (e.g.: `{ 'name': 'foo'}`)

![Simple query view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-queryFind.png?raw=true)

If you want to run some aggregate queries, type your pipeline as follows:
```js
{'$operator1': {...}},
{'$operator2': {...}}
```

![Aggregate query view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-queryAggregate.png?raw=true)

Typing **CTRL+SPACE** displays a popup that allows you to select a query operator.

Additionally, you can set a row limit.

When you are done, click on the Run button (or type **CTRL+ENTER** shortcut) to see the query results.

**Note**: If you use Ultimate Edition, JSON syntax highlighting is enabled.

## Document editing

Any document can be edited by double-clicking on the **object id** (or by right-clicking). A panel will open at the bottom.

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-documentEdition.png?raw=true)

You can edit the value either by double-clicking or by typing F2.
You can delete a key by right-clicking on it and select **Delete this**
* Click on the **save** button to send the modification to the mongo server
* Click on the **delete** button to delete it
You can add a key or value (depending on the structure of your document) by right-clicking on it and select **Add a Key** (or **Add a Value**). A dialog will appear.

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-addKeyDialog.png?raw=true)

Set the key name, type and value and then validate your form.

You can also edit a document from scratch by right-clicking in the result view and select *Add* (or by typing **ALT+INSERT** shortcut)

### Mongo shell integration

If you set the mongo client path (e.g., /usr/bin/mongo), you can run the console by clicking the menu item **Tools -> Mongo Shell**.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-shell.png?raw=true)

### Run a file

If you need to run a JavaScript file on your mongo server, just type CTRL+SHIFT+F10 (or right-clik in your file and select Run *myscript.js* file)

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-runAFile.png?raw=true)

Select your server and your database then click on the run button.


## Thanks

I would like to thank:
* MongoDB team
* Mongo Java Driver team
* Jetbrains Team for the great sources of IntelliJ Community Edition which help me to improve this plugin
* Mark James author of the famfamfam web site who provides beautiful icons.
* [Jean Baptiste Potonnier](https://github.com/jbpotonnier) for the suggestion to create this plugin
* [piddubnyi](https://github.com/piddubnyi) for adding [*drop database* action](https://github.com/dboissier/mongo4idea/pull/95)
* Neko team, my first beta testers ;)
* All users of this plugin who sent me valuable suggestions.
* My wife and my daughters who support me to have fun in software development and also remind me my husband/father duty ;).


## FAQ


### The plugin does not work. I have `ClassNotFoundDefException`

Some bugs were reported when installing the SNAPSHOT version of the plugin. Mainly some `ClassNotFoundDefException`. This could happen whenever you have a previous version of the plugin already installed in the IDE.
To fix it, the steps should be as follow:

* Locate the IDE working directory. For WebStorm, it is `.WebStorm` and for Intellij, it is `.IntelliJIdea`
* In it, remove the snapshot and the stable versions from `<working_dir>/config/plugins`
* Download again the right version for your target IDE
* Deflate the archive in the same folder
* You should have one instance of the plugin: `<working_dir>/config/plugins/mongo4idea-0.X.Y-SNAPSHOT`

Restart your IDE and it should be ok :).
