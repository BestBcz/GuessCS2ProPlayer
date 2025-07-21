## 仿Blast.tv猜CS2职业选手的小游戏插件-GuessCS2ProPlayer
https://blast.tv/counter-strikle/multiplayer
### [基于miraiQQ机器人](https://github.com/mamoe/mirai)

[![Github](https://img.shields.io/badge/-Github-000?style=flat&logo=Github&logoColor=white)](https://github.com/BestBcz/GuessCS2ProPlayer)
[![MiraiForum](https://img.shields.io/badge/Forum-Mirai?style=flat-square&label=Mirai
)](https://mirai.mamoe.net/user/kymandu)

-------------------------
#### 🌱 插件难产中...插件处于开发阶段  | 有任何建议或者想法可以在issue中提出

---------------------------
### 🛠️安装&依赖前置
1. 从Release中下载最新版本
2. 将Zip文件解压并放入 _%mirai文件根目录%/_ 中
3. 重新启动你的mirai-console
4. 在群内输入 _/猜选手 开始_ 开始游戏
#### 🛠️依赖前置（请安装到你搭建机器人的服务器上）
-  [mirai-skia-plugin](https://github.com/cssxsh/mirai-skia-plugin)
- [Mirai-console 2.16版本或以上](https://github.com/mamoe/mirai/releases)
- [Chat-Command](https://github.com/project-mirai/chat-command)


----------------------------------------

### 🚀代办清单 （可以订阅Release来获取更新)

- [ ] 制作完整版

-------------------------------------------------

### 🧐已拥有的功能

- [x] 基本的猜选手小游戏实现
- [x] 国旗显示支持
- [x] 200+个选手数据
- [x] 排行榜
- [x] 多种模式（bo3，bo5）

--------------------------------------------------

### 🔑指令
- /猜选手 开始 （default，bo3，bo5）
- /猜选手 结束
- /猜选手 排行榜
- /猜选手 resetleaderboard
- /开始猜选手

_指令需要给予相关权限才可以使用_
- org.bcz.guesscs2proplayer.command.猜选手.开始
- org.bcz.guesscs2proplayer.command.猜选手.结束
- org.bcz.guesscs2proplayer.command.猜选手.排行榜
- org.bcz.guesscs2proplayer.command.猜选手.resetleaderboard
- org.bcz.guesscs2proplayer.command.* （省事但是不建议，可能被恶意删榜）
- org.bcz.guesscs2proplayer.command.开始猜选手

-----------------------------------------
### 📷插件截图

![image](https://github.com/user-attachments/assets/ac274482-30bf-4bdb-befe-86940054cb03)

-------------------------------------

#### 💡 选手数据整理为csv文件放置在/data/org.bcz.guesscs2proplayer/中
#### 💡 目前使用的爬取方法为我自己的python项目，效果一般
#### 💡如有较为完美的思路或者程序可以在issue中告诉我

- 💡[prodown](https://github.com/BestBcz/prodown)
-------------------------------------------
### 编译  - 非开发者请无视
- 如果需要使用pluginbuild 编译请使用
- ```javascript
  ./gradlew clean buildPlugin -x miraiPrepareMetadata
  
- 来防止miraiPrepareMetadata造成的报错(理论上普通build也可行)