# Mongo Plugin for IntelliJ IDEA version 0.6.0

see [CHANGELOG.txt](https://github.com/dboissier/mongo4idea/blob/mongo4idea-0.6.0/CHANGELOG.txt)

Current [changelog](https://github.com/dboissier/mongo4idea/blob/master/CHANGELOG.txt)

## Description
This plugin integrates MongoDB Servers with database/collections tree,  Query Runner and Shell console.

## Plugin Compatibility
This plugin was built with JDK 1.6 and ideaIC-12.1.4 version (Idea 11 is not supported anymore).

## How to install it?
Download this plugin from your IDE (Mongo Plugin)

**Warning** : configurations will be broken, especially server url. You will have to set it again.

## Configuration steps

When you open the Mongo explorer, you will see an panel:

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithoutDB.png?raw=true)

* To manager your Mongo servers, click on the Mongo Settings button located on the upper toolbar of the Mongo explorer Right Panel

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-configuration.png?raw=true)

* You can edit your mongo shell path with a test button
* To add a server, click on the **[+]** button and a dialog will appear
* Set a label for your server, this will be more convenient to distinguish each of them in the Mongo explorer
* Set the server info as the example in the above screenshot
* If your access is restricted to a specific database, just type it in the corresponding field
* Put your credentials if your server requires authentication
* You can let the plugin connect to the server on the IDE startup by clicking on the corresponding checkbox
* If you want to hide some collections, you can put them in the **Collections to ignore** field.
* You can click on the **Test Connection** button to check your server configuration

## Usage

### Mongo Explorer

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithDB.png?raw=true)

The tree displays for each server all databases with its collections. Just double-click on a collection and the results will be displayed as same as a file tab.
If you double-click on another collection, a new tab will appears side of the previous.

If you clear a collection by right clicking on it and select **Drop collection**

### **[NEW]** Mongo collection tab view

The panel shows all documents of the collections (max 300 records by default).

![Collection view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-collectionTabView.png?raw=true)

You can copy the result and paste in a text editor.

#### Querying

If you want to run a *find* query, click on the Magnify icon or type **CTRL+F** shortcut.

The query panel will appear on the top.

Type your find query in JSON format (e.g.: `{ 'name': 'foo'}`)

![Simple query view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-queryFind.png?raw=true)

If you want to run some aggregate queries, type your pipeline as following:
```
{'$operator1': {...}},
{'$operator2': {...}}
```

![Aggregate query view](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-queryAggregate.png?raw=true)

When you type **CTRL+SPACE** key shortcut a popup is displayed in which you can select a query operator.

Additionally, you can set a row limit.

When you are done, click on the Run button (or type **CTRL+ENTER** shortcut) to see the query results.

**Note**: If you use Ultimate Edition, JSON syntax highlighting is enabled.

## Document edition

Any document can be edited by double-clicking on the **object id** (or by right-clicking). A panel will be opened on the bottom.

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-documentEdition.png?raw=true)

You can edit the value either by double-clicking or by typing F2 key.
You can delete a key by right-clicking on it and select **Delete this**
* Click on the **save** button to send the modification to the mongo server
* Click on the **delete** button to delete it
You can add a key or value (depending on the structure of your document) by right-clicking on it and select **Add a Key** (or **Add a Value**). A dialog will appear.

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-addKeyDialog.png?raw=true)

Set the key name, type and value and then validate your form.

**[NEW]** You can also edit a document from scratch by right-clicking in the result view and select *Add* (or by typing **ALT+INSERT** shortcut)

### Mongo shell integration

If you set the mongo client path (e.g. /usr/bin/mongo), you can run the console by clicking the menu item **Tools -> Mongo Shell**.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-shell.png?raw=true)

### **[NEW]** Run a file

If you need to run a javascript file on your mongo server, just type CTRL+SHIFT+F10 (or right-clik in your file and select Run *myscript.js* file)

![Document edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-runAFile.png?raw=true)

Select your server and your database then click on the run button.


## Thanks

I would like to thank:
* MongoDB team
* Mongo Java Driver team
* Jetbrains Team for the great sources of IntelliJ Community Edition which help me to improve this plugin
* Mark James author of the famfamfam web site who provides beautiful icons.
* [Jean Baptiste Potonnier](https://github.com/JJeeb) for the suggestion to create this plugin
* Neko team, my first beta testers ;)
* All users of this plugin who sent me valuable suggestions.
* My wife and my daughters who support me to have fun in software development and also remind me my husband/father duty ;).


### Last Developer notes


#### Why could the document be edited directly in the tree result?

After making some tries, many issues were found:
* Sometimes, projection can be used in the query. Edition requires having the object **id** to send the updated value to the mongo server and have all Mongo object content.
* When the user updates a value, it was sent directely to the server. It is not convenient and does not handle misstyping. The user would like to update a set of key value.

So, I decided to make a specific GUI for it:
* Easier to make and test
* Component non coupled with the result tree.
* The user has the full control on the update/save operations


### The plugin does not work. I have `ClassNotFoundDefException`

Some bugs were reported when installing the SNAPSHOT version of the plugin. Mainly some `ClassNotFoundDefException`. This could happen whenever you have a previous version of the plugin already installed in the IDE.
To fix it, the steps should be as follow:
* Locate the IDE working directory. For WebStorm, it is `.WebStorm` and for Intellij, it is `.IntelliJIdea`
* In it, remove the snapshot and the stable versions from `<working_dir>/config/plugins`
* Download again the right version for your target IDE
* Deflate the archive in the same folder
* You should have one instance of the plugin: `<working_dir>/config/plugins/mongo4idea-0.X.Y-SNAPSHOT`

Restart your IDE and it should be ok :).
