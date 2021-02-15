# TableauTracker Events as Datasource Columns in Tableau

​	After retrieving the data through the Tableau Tracker Web Data Connector we have various type of data recorded based on the type of the event. The two base tyes of events we records are: 

- NOOP: Heartbeat info sent in every 15 seconds by the app to know when the dashboard is opened.
- FILTER_CHANGE: Raised if a filter is changed on the dashboard.
- FILTER_STATE: The app sends the filter state of all worksheets of the current dashboard at every 5 seconds.

There are Basic Columns, which are present at all types of events, and specific columns, which are present only according to a type of event.

## Basic Columns

| *Column name*              | *Explanation*                                                |
| -------------------------- | ------------------------------------------------------------ |
| **deploymentId**           | The Deployment ID recieved during the registration.          |
| **sourceId**               | The ID of one Session. Can be used to identify the changes generated in one session. |
| **sourceSequenceId**       | The number of the event within a Session.                    |
| **dashboardName**          | The name of the dashboard which was the source of the event. |
| **workbookName**           | The name of the workbook which was the source of the event. (Should be defined during the configuration of the extension.) |
| **data_sheet**             | The sheet which was the source of this event.                |
| **recordedAt**             | The exact time of the event.                                 |
| **kind**                   | The type of the event. Either: NOOP, FILTER_CHANGE or FILTER_STATE. |
| **data_document_referer**  | URI of the page that linked to the page where the extension is running. |
| **data_document_location** | The entire URL of the page where te event is recorded.       |
| **data_window_height**     | The height of the window during the occurance of the event.  |
| **data_window_width**      | The width of the window during the occurance of the event.   |

## Filter change columns

| *Column name*                         | *Explanation*                                                |
| ------------------------------------- | ------------------------------------------------------------ |
| **data_fieldName**                    | The name of the field being filtered. Note that this is the caption as shown in the UI, and not the actual database field name. |
| **data_filterType**                   | The type of the filter. Either categorical, hierarchical, range or relative-date. |
| **data_appliedValues_value**          | The value applied to a categorical filter. For old and new value, two separate rows will be genarted. |
| **data_appliedValues_formattedValue** | The formatted value applied to a categorical filter.         |
| **data_minValue_value**               | Minimum value, inclusive, applied to the filter.             |
| **data_minValue_formattedValue**      | Minimum formatted value applied to the filter.               |
| **data_maxValue_value**               | Maximum value, inclusive, applied to the filter.             |
| **data_maxValue_formattedValue**      | Maximum formatted value applied to the filter.               |
| **data_includeNullVales**             | True if null values are included in the filter, false otherwise. |
| **data_isExcludeMode**                | True if this filter is an exlucde filter, false if include filter. Include means that you use the fields as part of a filter. Exclude mode means that you include everything else except the specified fields. |

## Custom columns

​	Extra settings can be defined in the extension configuration, which can be retrieved in the Web Data Connector. These settings represented as columns with "*custom_*" postfix. 

​	For example, if you added an extra setting in the extension named "mode", with value "debug", and add "mode" in the Web Data Connector custom metadata fields section, then the datasource will have a column named "custom_mode" with value "debug" in every event row from this session.