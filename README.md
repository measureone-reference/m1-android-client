# Android WebView Demo with M1 Widget Integration

A sample Android application that demonstrates how to integrate the MeasureOne (M1) widget into a WebView with dynamic configuration and bidirectional communication.

## Features

- **Dynamic Configuration**: Form-based input for access token and data request ID
- **WebView Integration**: Loads M1 widget HTML with JavaScript enabled
- **Bidirectional Communication**: Android ↔ WebView communication via JavaScript interface
- **Event Handling**: Captures M1 widget events (datasourceConnected, etc.)
- **Modern UI**: Material Design form interface with validation
- **Real-time Feedback**: Toast messages and logging for configuration status

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/webviewdemo/
│   │   └── MainActivity.java          # Main activity with WebView and form handling
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml      # Layout with form and WebView
│   │   ├── values/
│   │   │   ├── strings.xml            # String resources
│   │   │   ├── colors.xml             # Color definitions
│   │   │   └── themes.xml             # App theme
│   │   ├── drawable/
│   │   │   └── form_background.xml    # Form background styling
│   │   └── xml/
│   │       ├── backup_rules.xml       # Backup configuration
│   │       └── data_extraction_rules.xml # Data extraction rules
│   └── assets/
│       ├── m1_widget.html             # M1 widget HTML (primary)
│       └── sample.html                # Sample HTML for testing
├── build.gradle                       # App-level build configuration
└── proguard-rules.pro                 # ProGuard rules
```

## Quick Start

### Prerequisites

- Android Studio 4.0+
- Android SDK API Level 21+ (Android 5.0+)
- Java 8 or higher

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/measureone-reference/android-client.git
   cd android-client
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Choose the cloned directory

3. **Build and Run**:
   - Let Gradle sync complete
   - Run on an emulator or device

### Usage

1. **Launch the app** - You'll see a form with two input fields
2. **Enter your credentials**:
   - **Access Token**: Your M1 access token
   - **Data Request ID**: Your M1 data request ID
3. **Click "Load M1 Widget"** - The app will:
   - Validate your inputs
   - Load the M1 widget HTML
   - Inject your configuration
   - Display the widget in the WebView

## Configuration

### M1 Widget Configuration

The app dynamically injects configuration into the M1 widget:

```javascript
var config = {
    access_key: 'YOUR_ACCESS_TOKEN',
    host_name: 'api-stg.measureone.com',
    datarequest_id: 'YOUR_DATA_REQUEST_ID',
    branding: {
        styles: {
            primary_dark: '#186793',
            primary_light: '#2e9ccb',
            secondary_color: '#ffffff',
            min_height: '700px'
        }
    },
    options: {
        'display_profile': false
    }
};
```

### WebView Settings

- JavaScript enabled
- DOM storage enabled
- File access allowed
- Zoom controls enabled
- Wide viewport support

## Communication Flow

### Android → WebView
```java
// Inject configuration
webView.evaluateJavascript(configScript, null);

// Execute JavaScript functions
webView.evaluateJavascript("javascript:functionName()", null);
```

### WebView → Android
```javascript
// Send messages to Android
Android.onMessageReceived("Hello from WebView!");

// Report widget events
Android.onDatasourceConnected(eventData);

// Show Android toast
Android.showToast("Widget message");
```

## Event Handling

The app captures and handles various M1 widget events:

- **Configuration Success**: Toast when config is injected
- **Datasource Connected**: Toast when M1 widget connects
- **Error Handling**: Validation errors and configuration issues
- **Widget Events**: All M1 widget lifecycle events

## Customization

### Adding New Configuration Fields

1. Add input field to `activity_main.xml`
2. Update `MainActivity.java` to capture the new value
3. Modify `injectConfiguration()` to include the new field

### Modifying Widget Styling

1. Update the `branding.styles` object in `injectConfiguration()`
2. Modify colors in `colors.xml`
3. Update theme in `themes.xml`

### Adding New Events

1. Add event listener in the HTML file
2. Create corresponding method in `WebAppInterface` class
3. Handle the event in `MainActivity`

## Troubleshooting

### Common Issues

1. **JavaScript not working**: Ensure `setJavaScriptEnabled(true)` is called
2. **Configuration not loading**: Check access token and data request ID format
3. **Widget not appearing**: Verify M1 widget HTML is loading correctly
4. **Events not captured**: Check JavaScript interface is properly added

### Debug Tips

- Use Android Studio's Logcat to view Android logs
- Check web console logs in the WebView
- Monitor toast messages for configuration status
- Use `adb logcat | grep WebViewDemo` for filtered logs

## Permissions

The app requires the following permissions:
- `INTERNET`: For web content loading
- `ACCESS_NETWORK_STATE`: For network state monitoring

## Security Considerations

- JavaScript interface is properly secured
- File access is limited to app assets
- Network access is controlled
- Configuration validation prevents invalid inputs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is provided as a reference implementation for MeasureOne widget integration. Please refer to MeasureOne's terms of service for usage guidelines.

## Support

For issues related to:
- **M1 Widget**: Contact MeasureOne support
- **Android Implementation**: Open an issue in this repository
- **General Questions**: Check the documentation or create a discussion

---

**Note**: This is a demo application. For production use, ensure proper security measures, error handling, and user experience considerations are implemented. 