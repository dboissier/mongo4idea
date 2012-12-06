# Jenkins Plugin for IntelliJ IDEA

## ChangeLog
See CHANGELOG.txt

## Description
This plugin integrates MongoDB Server with database/collections tree and Query Runner

## Plugin Compatibility
This plugin was built with JDK 1.6 and ideaIU-11.1.4 version.

## Installation steps
Download this plugin from your IDE (Mongo Plugin)

## Configuration steps
* The plugin intends to connect to the local Mongo server with default parameters (127.0.0.1/27017)
* If you need to specify another server, click on the Mongo Settings button located on the upper toolbar of the Mongo explorer Right Panel
* If the server requires authentication, you can put your credentials
* If you want to hide some collections, you can put them in the **Collections to ignore** field.
* You can click on the **Test Connection* button to check your server configuration

## Usage

### Mongo Explorer
The tree displays for each database all collections. Just double-click on a collection and the results will be displayed in the Mongo Runner Panel.

### Mongo Runner
The panel is divided into 2 parts.

* Right part displays the results of the query in a tree (max. 300 records).
You can copy the result and paste in a text editor.

Left part allows to specify a query (in json format).
When you type CTRL+SPACE key shortcut a popup is displayed in which you can select query operator.
Either you click on the run query button or else type CTRL+F5 shortcut to run the query
If your server version is at least 2.2, you can use the aggregation framework. You can add a pipeline operation by clicking on the [+] button.
You can also copy/paste the query you wrote.

## Thanks
I would like to thank:
* MongoDB team
* Mongo Java Driver team
* Jetbrains Team for providing us such an incredible IDE (certainly the best that Java developers could have).
* Mark James author of the famfamfam web site who provides beautiful icons.
* Jean Baptiste Potonnier for the suggestion to create this plugin
* Neko team, my first beta testers ;)
* My wife and my daughter who support me to have fun in software development and also remind me my husband/father duty ;).
