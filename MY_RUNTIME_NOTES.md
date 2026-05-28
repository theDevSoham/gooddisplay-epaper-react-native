## Understanding the data (legacy codebase)

- The `MainActivity.java` holds the bridge between UI XML and functionality
- In android, UI and Functionality is analogus to HTML and Javascript. In UI there are a list of radio buttons for 2,3 and 4 color boards.
- There's also a list of sizes of screens. Based on the sizes, the list shows buttons. Ids are attached to the buttons as well.
- When the user selects a combo of these color and dimension buttons, an epd array gets filled
- epd array lives in the public `data` class and can be set using a setter

### The `data` class

- first is the coordinates for image rendering. x,y,w and h.
- next are imageview width and height (irrelevant for us as it's a ui logic)
- E paper related params: Extremely important -> epdArray of size 10, with W 0 1 and H 2 3 (don't know what it means). epd means e paper display I'm guessing
- a scaling ratio
- epd model string. It shows please select but the comment says it's defaulted to GDEH0154D67. What is GDEH0154D67?
- From internet found that GDEH0154D67 is the model number for 1.54” E-PAPER DISPLAY.
- Didn't find any context about the epdModel used throughout the data folder. dunno where it is configured or used.
- Checked this method

```java
    public static void setEpdString(String EpdArray) {
        data.epdmodel = EpdArray;
    }
```

this is setting epdModel to epdArray.

- This code is also doing the same with epdArray

```java
    public static void setEpdArray(int[] EpdArray) {
        data.epdArray = EpdArray;
    }
```

- Weird contradiction
- Let's continue.
- EPD Parameters are really essential. Maybe they need to be copied as is.
- Apart from all the getters and setters, the mainly important is setEpdInit. This initializes the epd as per I know. Let's grill this.
- It takes epd color and inch and is called from activity_imageview. Let's go down later then.
- The main part that is necessary till now is that Mainactivity sets the epd array and moves intent to activity_imageview.

### The activity imageview

- It has a lot of variables which I do not care.I want the workflow
- It enters on onCreate. It sets which image to show based on the choices. (Completely irrelevant for us as we are gonna upload dynamic images)
- Other important functions are - getBitmapFromImageView: converts image view to bitmap (do we need this? I'm not sure), GetPictureData_SSD: It is supposed to convert bitmap to array. Now I don't know why, GetPictureData_4G: Vertical scanning for all epapers. Also don't know why, writeTag: Probably the most important for us, startRefreshMonitoring and stopRefreshMonitoring which are for refresh monitoring, and some helpers if I'm not wrong like delay_ms, rotateBitmap, HexToString, NumToString, parse and HexString2Btyes
- All this is to now tally what the agent has done till now.

## Understanding the agent code - module for expo

- This is the major pain point as I don't know what's the entry point for the vertical slices. They seem to be independent.
- Let me check independently.

### checkpoint 1 (probably will help in checking what it did)

- Testing these files

```md
| Path                                           | Role                                                  |
| ---------------------------------------------- | ----------------------------------------------------- |
| `services/ImageProcessor.java`                 | Decode, scale, rotate, pack, `process()`              |
| `services/ImageProcessingParity.java`          | Buffer validation + legacy reference packer for tests |
| `models/ProcessedImage.java`                   | `blackLayer`, `colorLayer`, `width`, `height`         |
| `models/ImageProcessingConfig.java`            | Panel width/height, inch, color mode                  |
| `models/ColorMode.java`                        | Legacy values 2 / 3 / 4                               |
| `protocol/HexCodec.java`                       | `hexStringToBytes`, `bytesToHexString`                |
| `android/src/test/.../ImageProcessorTest.java` | Robolectric parity tests                              |
| `android/src/test/.../HexCodecTest.java`       | Hex round-trip                                        |
```

- I don't think I fully understand ImageProcessor.java and where in the world it came from activity_imageview
