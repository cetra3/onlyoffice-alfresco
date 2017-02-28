# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [1.2.8] 2017-02-28

* Above Preview Threshold message is changed

## [1.2.7] 2017-02-28

### Changed

* Optimisations to XLSX threshold reader & LRU Cache for threshold lookup

## [1.2.6] 2017-02-13

### Changed

* No longer updating date/time when locking document (Reverts 1.2.3)
* Refactored the thresholds code

## [1.2.5] 2016-12-14

### Changed

* Documents that are above preview thresholds will no longer be previewed in Share
* Thresholds values can be set accordingly in alfresco-global.properties, the example settings are:
```
  onlyoffice.preview.document.size.threshold=10485760
  onlyoffice.preview.docx.threshold=8000
  onlyoffice.preview.doc.threshold=8000
  onlyoffice.preview.xlsx.threshold=10000
  onlyoffice.preview.xls.threshold=10000
  onlyoffice.preview.pptx.threshold=1000
  onlyoffice.preview.ppt.threshold=1000
```
* In the example settings above, document size threshold is 10Mb, the docx and doc threshold is max paragraphs number, the xlsx and xls threshold is max total rows of sheets, the pptx and ppt thresholds is max slides number.
* Any threshold's value will be 0 if they are missing in properties file. Therefore the corresponding threshold check won't be performed. For example, if `onlyoffice.preview.document.size.threshold` is not present in alfresco-global.properties, then there won't be any limitation in terms of document size when user tries to preview a document.

## [1.2.4] 2016-11-23

### Fixed

* Mime types can't be previewed in OnlyOffice

### Changed

* Lock banner messages now allow user to distinguish the document is locked by OnlyOffice or other program in document library

## [1.2.3] 2016-11-22

### Changed

* Modification Date is updated if the document is locked (Alfstream Compatibility)

### Added

* Global property for the PDF transform url, which can be seperate from the editor URL: `onlyoffice.transform.url`

## [1.2.2] 2016-09-09

### Fixed

* The `Edit in OnlyOffice` action appears only if the document is unlocked or currently being edited inside onlyoffice.  This prevents the action showing if it's locked by something external (I.e, edit offline or edit in microsoft).

## [1.2.1] 2016-08-31

### Fixed

* Preview for External Share

## [1.2.0] 2016-08-23

### Added

* OnlyOffice Web Preview in Alfresco Share
* OnlyOffice PDF Conversion for Office Documents

## [1.1.0] 2016-07-20

### Added

* Keep alive to make sure the share session does not expire when you have a window open
* Languages as per the OnlyOffice fork
* New global property: `onlyoffice.lang`.  This will set the language of the editor.

### Fixed

* Compatibility support for OnlyOffice 4.0

### Changed

* Adjusted Mime Type Evaluator to certain mime types within Alfresco

## [1.0.3] 2016-02-03

### Fixed

* Use UTF-8 encoding for international characters

### Changed

* Target Java 1.7 for Compatibility

## [1.0.2] 2015-11-06

### Added

* Italian Localization

### Fixed

* More robust handling of multi-user documents

## [1.0.1] 2015-10-20

### Changed

* Converted to Static Module

## [1.0.0] 2015-08-16

* Initial Commit
