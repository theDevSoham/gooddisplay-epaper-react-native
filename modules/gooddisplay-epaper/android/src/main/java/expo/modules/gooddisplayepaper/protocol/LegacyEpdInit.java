package expo.modules.gooddisplayepaper.protocol;

/**
 * Port of {@code data.data.setEpdInit} — local variables only, no static mutation.
 * Generated for protocol parity; do not edit by hand unless syncing from legacy {@code data.java}.
 */
final class LegacyEpdInit {

  private LegacyEpdInit() {}

static String[] setEpdInit(int epdColor, int epdInch) {
        String[] epd_init = new String[10];
        ;
        if (epdColor == 2) {
            switch (epdInch) {
                /****************************************************UC Series Monochrome Screen**************************************************************/
                //Screen size classification: 0.97-0111 1.54-0010   2.13-0000   2.66-0110  2.7-0100 2.9-0001 3.1-0101  3.7-0101  4.2- 0011
                case 97: {
                    //0.97 88x184  SSD1680
                    String data_DB1 = "F0DB000067";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0060720005800B8";//16   0.97 inch monochrome 88x184
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000A" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00720";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 153: {
                    String data_DB1 = "F0DB000062";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006122000980098";//16   1.54 inch monochrome 152x152
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401970001";//12
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440012" + "A1054597000000";//24
                    String set_border1 = "A1023C05";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F9700";//18
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F01220";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 154: {
                    String data_DB1 = "F0DB000062";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006122000C800C8";//16   1.54 inch monochrome 200x200
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401C70001";//12
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440018" + "A10545C7000000";//24
                    String set_border1 = "A1023C05";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034FC700";//18
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F01220";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 213: {
                    //2.13 122x250  SSD1680
                    String data_DB1 = "F0DB000067";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0060020007A00FA";//16   2.13 inch monochrome 128x250
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000F" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00020";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 266: {
                    String data_DB1 = "F0DB000067";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006062000980128";//16   2.66 inch monochrome 152x296
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440012" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00620";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 270: {
                    String data_DB1 = "F0DB000062";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006042000B00108";//16   2.7 inch monochrome 176x264
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440015" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    //set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00420";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 290: //2.9
                    String data_DB1 = "F0DB000067";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006012000800128";//16   2.9 inch monochrome 128x296
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000F" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BW1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00120";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                case 420:  //Need to transfer data twice, put it into three-color mode
                    String data_DB1 = "F0DB000063";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A00603300190012C";//16   4.2 inch three-color 400x300
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A104012B0101";//12    //01 2B 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440031" + "A105452B010000";//24    //44 45
                    String set_border1 = "A1023C01";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2B01";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00330";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                case 370: {
                    //3.71 240x416  UC8171
                    String data_DB = "F0DB00005E";//10    The last digit is half of the sum of all subsequent data.
                    String start = "A006512000F001A0";//16  3.71 inch monochrome 240x416
                    String RST = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40103"; // 48 +56   Reset three times
                    String set_wf = "A102001F";//8  //0x00  0x1F
                    String set_resolution = "A10461F001A0";//12   //0x61 f0 01 a0
                    String set_power = "A10104" + "A40103";//12  //0x04  busy
                    String write_BW = "A3021013"; //8    //0x10
                    String update = "A20112" + "A502000A" + "A40103";//20  //0x12 delay  busy
                    String sleep = "A20102" + "A40103" + "A20207A5"; //20 //0x02 busy 07 A5
                    epd_init[0] = data_DB + start + RST + set_wf + set_power + write_BW + update + sleep;   //16+104+12+8+8+20+20=188   188/2=94=0x5E
                    //Screen switching
                    epd_init[1] = "F0DA000003F05120";//10   Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
            }
        }
        if (epdColor == 3) {
            switch (epdInch) {
                /****************************************************UC Series Monochrome Screen**************************************************************/
                //Screen size classification: 0.97-0111 1.54-0010   2.13-0000   2.66-0110  2.7-0100 2.9-0001 3.1-0101  3.7-0101  4.2- 0011
                case 97: {
                    //0.97 88x184  SSD1680
                    String data_DB1 = "F0DB000068";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0060730005800B8";//16   0.97 inch three-color 88x184
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000A" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00730";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 153: {
                    String data_DB1 = "F0DB000063";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006122000980098";//16   1.54 inch three-color 152x152
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401970001";//12
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440012" + "A1054597000000";//24
                    String set_border1 = "A1023C05";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F9700";//18
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F01230";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 154: {
                    //1.54 200x200  SSD1681
                    String data_DB1 = "F0DB000063";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006123000C800C8";//16    1.54 inch three-color 200x200
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401C70001";//12
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440018" + "A10545C7000000";//24
                    String set_border1 = "A1023C05";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034FC700";//18
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F01230";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 213: {
                    //2.13 122x250  SSD1680
                    String data_DB1 = "F0DB000068";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0060030007A00FA";//16   2.13 inch three-color 128x250
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000F" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00030";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 266: {
                    String data_DB1 = "F0DB000068";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006063000980128";//16   2.66 inch three-color 152x296
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440012" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00630";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 270: {
                    String data_DB1 = "F0DB000063";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006043000B00108";//16   2.7 inch three-color 176x264
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440015" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    //set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00430";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 290: //2.9
                    String data_DB1 = "F0DB000068";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006013000800128";//16   2.9 inch three-color 128x296
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A10401270101";//12    //01 27 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A10344000F" + "A1054527010000";//24    //44 45
                    String set_border1 = "A1023C05";//8
                    String set_RAM_Shift1 = "A103210080";//10
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2701";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_RAM_Shift1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00130";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                case 420: {
                    String data_DB1 = "F0DB000063";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A00603300190012C";//16   4.2 inch three-color 400x300
                    String RST1 = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40102"; //48
                    String soft_RST1 = "A10112" + "A40102";//14
                    String set_control1 = "A104012B0101";//12    //01 2B 01
                    String set_mode1 = "A1021101";//8
                    String set_RAM1 = "A103440031" + "A105452B010000";//24    //44 45
                    String set_border1 = "A1023C01";//8
                    String set_temperature1 = "A1021880";//8
                    String set_RAM_counter1 = "A1024E00" + "A1034F2B01";//18     //4E 4F
                    String write_BW1 = "A30124"; //6
                    String write_BWR1 = "A3022426"; //8
                    String update1 = "A20222F7" + "A20120" + "A40102";//20
                    String sleep1 = "A2021001" + "A502000A"; //16
                    epd_init[0] = data_DB1 + start1 + RST1 + soft_RST1 + set_control1 + set_mode1 + set_RAM1 + set_border1 + set_temperature1 + set_RAM_counter1 + write_BWR1 + update1 + sleep1;
                    //Screen switching
                    epd_init[1] = "F0DA000003F00330";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 370: {
                    //3.71 240x416  UC8171
                    String data_DB = "F0DB00005E";//10    The last digit is half of the sum of all subsequent data.
                    String start = "A006513000F001A0";//16  3.71 inch three-color 240x416
                    String RST = "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40103"; // 48 +56   Reset three times
                    String set_wf = "A102000F";//8  //0x00  0x0F
                    String set_resolution = "A10461F001A0";//12   //0x61 f0 01 a0
                    String set_power = "A10104" + "A40103";//12  //0x04  busy
                    String write_BW = "A3021013"; //8    //0x10
                    String update = "A20112" + "A502000A" + "A40103";//20  //0x12 delay  busy
                    String sleep = "A20102" + "A40103" + "A20207A5"; //20 //0x02 busy 07 A5
                    epd_init[0] = data_DB + start + RST + set_wf + set_power + write_BW + update + sleep;   //16+104+12+8+8+20+20=188   188/2=94=0x5E
                    //Screen switching
                    epd_init[1] = "F0DA000003F05130";//10   Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20   (Screen resolution and color are consistent with A0 command)
                    break;
                }
            }
        }
        if (epdColor == 4) {
            switch (epdInch) {
                /****************************************************UC Series Monochrome Screen**************************************************************/
                //Screen size classification: 0.97-0111 1.54-0010   2.13-0000   2.66-0110  2.7-0100 2.9-0001 3.1-0101  3.7-0101  4.2- 0011
                case 97: {
                    String data_DB1 = "F0DB000076";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006072000B000B8";//16   0.97-inch 4-color 88x184--176x184 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A10561005800B8";//14    //61 00 58 00 B8
                    String set_0xE7 = "";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB6 = "A102E71C";//8   //B6 6F
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x50 + set_0x60 + set_0x61 +
                            set_0xB6 + set_0xE3 + set_0xB4 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //236 /2=118=0x76
                    //Screen switching
                    epd_init[1] = "F0DA000003F00720";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 153: //1.54-inch 4-color low resolution 152x152
                    String data_DB1 = "F0DB00005A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006122001300098";//16   1.54-inch 4-color 152x152--304x152 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x06 = "A108060D123020192A22";//20  //06 0D 12 30 20 19 2A 22
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x61 = "A1056100980098";//14    //61 00 98 00 98
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08

                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x06 + set_0x50 + set_0x61 +
                            set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //180 /2=90=0x5A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00720";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                case 154: {
                    String data_DB1 = "F0DB00005A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0061220019000C8";//16   1.54-inch 4-color 200x200--400x200 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x06 = "A108060D123020192A22";//20  //06 0D 12 30 20 19 2A 22
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x61 = "A1056100C800C8";//14    //61 00 C8 00 C8
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08

                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x06 + set_0x50 + set_0x61 +
                            set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //180 /2=90=0x5A
                    //Screen switching
                    epd_init[1] = "F0DA000003F01220";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                //4-color screen  Conventional driver
                case 213: //New substrate (Test with reverse consistency to mobile phone during testing for the strongest signal)
                    String data_DB1 = "F0DB000038";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A0060020010000FA";//16   2.13-inch 4-color 128x250--256x250 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0xE9 + set_power + write_BW + update + sleep; //112 /2=56=0x38
                    //Screen switching
                    epd_init[1] = "F0DA000003F00020";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;

                case 290: {
                    String data_DB1 = "F0DB00007A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006012001000128";//16   2.9-inch 4-color 128x296--256x296 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100800128";//14    //61 00 80 01 28
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "A102B503";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01+ set_0x03+ set_0x06+ set_0x50+ set_0x60 + set_0x61+
                            set_0xE7 + set_0xE3 + set_0xB4 + set_0xB5 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //244 /2=122=0x7A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00120";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 291://2.9-inch high resolution
                    String data_DB1 = "F0DB00007A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006012001500180";//16   2.9-inch 4-color 168x384--366x384 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100A80180";//14    //61 00 A8 01 80
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "A102B503";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x50 + set_0x60 + set_0x61 +
                            set_0xE7 + set_0xE3 + set_0xB4 + set_0xB5 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //244 /2=122=0x7A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00120";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                case 266: {
                    String data_DB1 = "F0DB00007A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006062001300128";//16   2.66-inch 4-color 152x296--304x296 resolution scaled up by double
                    String RST1 = "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40103"; //34  //RST0 10ms RST1 10ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100980128";//14    //61 00 98 01 28
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "A102B503";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x50 + set_0x60 + set_0x61 +
                            set_0xE7 + set_0xE3 + set_0xB4 + set_0xB5 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //244 /2=122=0x7A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00620";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 267: //2.66-inch high resolution
                    String data_DB1 = "F0DB00007A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006062001700168";//16   2.66-inch high resolution 4-color 184x360--368x360 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100B80168";//14    //61 00 B8 01 68
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "A102B503";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x50 + set_0x60 + set_0x61 +
                            set_0xE7 + set_0xE3 + set_0xB4 + set_0xB5 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //244 /2=122=0x7A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00620";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                case 270: {
                    String data_DB1 = "F0DB00007A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006062001600108";//16   2.7-inch 4-color 176x264--352x264 resolution scaled up by double
                    String RST1 = "A40108" + "A502000A" + "A4010C" + "A502000A" + "A40103"; //34  //RST0 10ms RST1 10ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A103010700";//10    //01 07 00
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A1080605003F0A25121A";//20  //06 05 00 3F 0A 25 12 1A
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100B00128";//14    //61 00 B0 01 08
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xB4 = "A102B4D0";//8   //B4 D0
                    String set_0xB5 = "A102B503";//8   //B5 03
                    String set_0xE9 = "A102E901";//8   //E9 01
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x50 + set_0x60 + set_0x61 +
                            set_0xE7 + set_0xE3 + set_0xB4 + set_0xB5 + set_0xE9 + set_0x30 + set_power + write_BW + update + sleep; //244 /2=122=0x7A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00620";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 370: {
                    String data_DB1 = "F0DB0000A5";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A006512001E001A0";//16   3.7-inch 4-color 240x416--480x416 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY

                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x01 = "A10701070022780A22";//18    //01 07 00 22 78 0A 22
                    String set_0x03 = "A10403105444";//12   //03 10 54 44
                    String set_0x06 = "A10406C0C0C0";//12  //06 C0 C0 C0
                    String set_0x30 = "A1023008";//8   //30 08
                    set_0x41= "A1024100";//8   //41 00
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x60 = "A103600202";//10    //60 02 02
                    String set_0x61 = "A1056100F001A0";//14    //61 00 F0 01 A0
                    String set_0x65 = "A1056500000000";//14   //65 00 00 00 00
                    String set_0xE7 = "A102E71C";//8   //E7 1C
                    String set_0xE3 = "A102E322";//8   //E3 22
                    String set_0xFF1 = "A102FFA5";//8   //FF A5
                    String set_0xEF = "A107EF011E0A1B0B17";//18   //EF011E0A1B0B17
                    String set_0xC3 = "A108C3FDDC01DD08DE41";//20   //C3FDDC01DD08DE41
                    String set_0xFD = "A10301E803";//10   //01E803
                    String set_0xDA = "A102DA07";//8   //DA07
                    String set_0xC9 = "A102C900";//8   //C900
                    String set_0xA8 = "A102A80F";//8   //A80F
                    String set_0xFF2 = "A102FFE3";//8   //FF E3
                    String set_0xE9 = "A102E901";//8   //E901

                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1  + set_0x00 + set_0x01 + set_0x03 + set_0x06 + set_0x30 + set_0x41 + set_0x50 +
                            set_0x60 + set_0x61 + set_0x65 + set_0xE7 + set_0xE3 + set_0xFF1 + set_0xEF + set_0xC3 + set_0xFD + set_0xDA + set_0xC9 + set_0xA8 + set_0xFF2 + set_0xE9 + set_power + write_BW + update + sleep; //330 /2=165=0xA5
                    //Screen switching
                    epd_init[1] = "F0DA000003F05120";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
                case 420: {
                    String data_DB1 = "F0DB00006A";//10    The last digit is half of the sum of all subsequent data.
                    String start1 = "A00603300190012C";//16   4.2-inch 4-color 400x300--400x300 resolution scaled up by double
                    String RST1 = "A40108" + "A5020028" + "A4010C" + "A5020028" + "A40103"; //34  //RST0 40ms RST1 40ms BUSY
                    String set_0x4D = "A1024D78";//8   //4D 78
                    String set_0x00 = "A103000F29";//10    //00 0F 29
                    String set_0x06 = "A108060D122425122910";//20  //060D122425122910
                    String set_0x30 = "A1023008";//8   //30 08
                    String set_0x50 = "A1025037";//8   //50 37
                    String set_0x61 = "A10561012C0190";//14    //61012C0190
                    String set_0xAE = "A102AECF";//8   //AECF
                    String set_0xB0 = "A102B013";//8   //B013
                    String set_0xBD = "A102BD07";//8   //BD07
                    String set_0xBE = "A102BEFE";//8   //BEFE
                    String set_0xE9 = "A102E901";//8   //E9 01

                    String set_power = "A10104" + "A40103";//12  //04  busy
                    String write_BW = "A30110"; //6    //0x10
                    String update = "A2021200" + "A40103";//14  //12 00  busy
                    String sleep = "A2020200" + "A40103" + "A20207A5"; //22 //0x02 00 busy 07 A5
                    epd_init[0] = data_DB1 + start1 + RST1 + set_0x4D + set_0x00 + set_0x06 + set_0x30 + set_0x50 + set_0x61 +
                            set_0xAE + set_0xB0 + set_0xBD + set_0xBE + set_0xE9 + set_power + write_BW + update + sleep; //212 /2=106=0x6A
                    //Screen switching
                    epd_init[1] = "F0DA000003F00330";//10    Screen parameter 0000  Data length 03   Custom screen F0    Screen size resolution 12   Screen color 20 - 4-color  (Screen resolution and color are consistent with A0 command)
                    break;
                }
            }
        }
        return epd_init;
    }
}
