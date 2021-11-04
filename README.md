# MagicLamp: Controlling app for [GyverLamp](https://alexgyver.ru/gyverlamp/)
GyverLamp is electronic device based on LED matrix, contains lots of different light effects
<p></p>

![](https://giant4.ru/upload/resize_cache/iblock/5af/400_400_140cd750bba9870f18aada2478b24840a/5af1375b952472b6e72d38ba9a189861.jpg)

### Device principles
Matrix connected to NodeMCU module for Wi-Fi interaction with outer clients, and have sensor button on board
for manual user controlling
![](images/scheme0-2.jpg)

### Application overview
Now application has two main feature screens

#### Main screen
In the top of the screen there is connection status (if successful - current lamp IP address) and power button.
Main screen allows setting main effect's parameters - brightness, speed and scale, also it has list of available 
effects that we can refresh by swipe down.
<p></p>

![](images/main_screen.png)

#### Alarms screen
This feature allows user to set "dawn" alarm when lamp will gradually increase self brightness in particular time.
API of lamp isn't really handy and we can set only one alarm per day. In feature releases I plan to workaround this
constraint by client code.
<p></p>

![](images/alarms_screen.png)

#### Architecture
MVVM + Coroutines + UDP socket + Clean
Now it's contains only one monolithic module, but in the future I'll divide it in feature modules
