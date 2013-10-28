# Mongo Plugin for IntelliJ IDEA version 0.4.3

## Version 0.5.0-SNAPSHOT

[version for Idea 13 EAP and latest WebStorm 7](https://github.com/dboissier/mongo4idea/blob/master/snapshot/mongo4idea-0.5.0-SNAPSHOT-for-Idea13-distribution.zip?raw=true), [version for old Idea 11 and other IDE](https://github.com/dboissier/mongo4idea/blob/master/snapshot/mongo4idea-0.5.0-SNAPSHOT-distribution.zip?raw=true)

* [fix] Fatal Error with WebStorm 7 RC 131.130 (I had to compile with Idea 13 plateform, but without any changes, strange isn't it?)
* **NEW** [add] document can be edited (update value, save and delete document only)
* **NEW** [add] collections can be cleared (right click on the collection in the mongo explorer)

### Important notice

Some bugs were reported when installing the SNAPSHOT version of the plugin. Mainly some `ClassNotFoundDefException`. This could happen whenever you have a previous version of the plugin already installed in the IDE.
To fix it, the steps should be as follow:
* Locate the IDE working directory. For WebStorm, it is `.WebStorm` and for Intellij, it is `.IntelliJIdea`
* In it, remove the snapshot and the stable versions from `<working_dir>/config/plugins`
* Download again the right version for your target IDE
* Deflate the archive in the same folder
* You should have one instance of the plugin: `<working_dir>/config/plugins/mongo4idea-0.5.0-SNAPSHOT`

Restart your IDE and it should be ok :).

### Last developer notes on the document edition

Why could the document be edited directly in the tree result?

After making some tries, many issues were found:
* Sometimes, projection can be used in the query. Edition requires having the object **id** to send the updated value to the mongo server and have all Mongo object content.
* When the user updates a value, it was sent directely to the server. It is not convenient and does not handle misstyping. The user would like to update a set of key value.

So, I decided to make a specific GUI for it:
* Easier to make and test
* Component non coupled with the result tree.
* The user has the full control on the update/save operations


### ChangeLog of the stable 0.4.3

see [CHANGELOG.txt](https://github.com/dboissier/mongo4idea/blob/mongo4idea-0.4.3/CHANGELOG.txt)


## Description
This plugin integrates MongoDB Servers with database/collections tree,  Query Runner and Shell console.

## Plugin Compatibility
This plugin was built with JDK 1.6 and ideaIU-11.1.5 version.

## How to install it?
Download this plugin from your IDE (Mongo Plugin)

## Configuration steps

When you open the Mongo explorer, you will see an panel:

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithoutDB.png?raw=true)

* To manager your Mongo servers, click on the Mongo Settings button located on the upper toolbar of the Mongo explorer Right Panel

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-configuration.png?raw=true)

* You can edit your mongo shell path with a test button
* To add a server, click on the **[+]** button and a dialog will appear
* Set a label for your server, this will be more convenient to distinguish each of them in the Mongo explorer
* Set the server info as the example in the above screenshot*
* If your access is restricted to a specific database, just type it in the corresponding field
* Put your credentials if your server requires authentication
* You can let the plugin connect to the server on the IDE startup by clicking on the corresponding checkbox
* If you want to hide some collections, you can put them in the **Collections to ignore** field.
* You can click on the **Test Connection** button to check your server configuration

## Usage

### Mongo Explorer

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithDB.png?raw=true)

The tree displays for each server all databases with its collections. Just double-click on a collection and the results will be displayed in the Mongo Runner panel.
If you double-click on another collection, a new tab will appears side of the previous.
If you clear a collection by right clicking on it and select **Drop collection**

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-multipleTab.png?raw=true)

### Mongo Runner
The panel is divided into 2 parts.

* Right part displays the results of the query in a tree (max. 300 records).

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-findAll.png?raw=true)

You can copy the result and paste in a text editor.

* Left part allows to specify a query (in json format).

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-findWithFilter.png?raw=true)

When you type **CTRL+SPACE** key shortcut a popup is displayed in which you can select query operator.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-operatorPopup.png?raw=true)

Either you click on the run query button or else type **CTRL+ENTER** shortcut to run the query

If your server version is at least 2.2, you can use the aggregation framework. You can add a pipeline operation by clicking on the [+] button.
You can also copy/paste the query you wrote.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-aggregation.png?raw=true)

**NEW** By default, Aggregation query is enable. you can switch to find query by clicking on the tooggle button:

![Switch to Find/Aggregation Query](https://raw.github.com/dboissier/mongo4idea/master/doc/mongo4idea-switchFindAggregationQuery.png)

If you have an error during query execution, a feedback panel is displayed below:

* In case of bad JSON syntax

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-errorInExecution.png?raw=true)

* In case of general error

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-errorInExecutionCommand.png?raw=true)

**Note**: If you use Ultimate Edition, JSON syntax highlighting is enabled.

## **NEW** Document edition

Any document can be edited by clicking the **object id**. A panel will be opened on the right.

![Document Edition](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-documentEdition.png?raw=true)

You can edit the value either by double-cliking or by typing F2 key.

* Click on the **save** button to send the modification to the mongo server
* Click on the **delete** button to deletee it


### Mongo shell integration

If you set the mongo client path (e.g. /usr/bin/mongo), you can run the console by clicking the menu item **Tools -> Mongo Shell**.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-shell.png?raw=true)


## Thanks
I would like to thank:
* MongoDB team
* Mongo Java Driver team
* Jetbrains Team for the great sources of IntelliJ Community Edition which help me to improve this plugin
* Mark James author of the famfamfam web site who provides beautiful icons.
* [Jean Baptiste Potonnier](https://github.com/JJeeb) for the suggestion to create this plugin
* Neko team, my first beta testers ;)
* All users of this plugin who sent me valuable suggestions.
* My wife and my daughter who support me to have fun in software development and also remind me my husband/father duty ;).
