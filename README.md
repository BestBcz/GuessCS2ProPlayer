
# 此插件不会再更新，可随意借鉴参考修改分发。

# 仿Blast.tv猜CS2职业选手的小游戏插件-GuessCS2ProPlayer

一个基于 Mirai Console 的 CS2 职业选手猜猜猜游戏插件。
(插件处于开发阶段，当前为测试版发行)
https://blast.tv/counter-strikle/multiplayer
### [基于miraiQQ机器人](https://github.com/mamoe/mirai)

[![Github](https://img.shields.io/badge/-Github-000?style=flat&logo=Github&logoColor=white)](https://github.com/BestBcz/GuessCS2ProPlayer)
[![MiraiForum](https://img.shields.io/badge/Forum-Mirai?style=flat-square&label=Mirai
)](https://mirai.mamoe.net/user/kymandu)

### 🛠️安装&依赖前置
1. 从Release中下载最新版本
2. 将Zip文件解压并放入 _%mirai文件根目录%/_ 中
3. 重新启动你的mirai-console
4. 在群内输入 _/猜选手 开始_ 开始游戏
#### 🛠️依赖前置（请安装到你搭建机器人的服务器上）
-  [mirai-skia-plugin](https://github.com/cssxsh/mirai-skia-plugin)
- [Mirai-console 2.16版本或以上](https://github.com/mamoe/mirai/releases)
- [Chat-Command](https://github.com/project-mirai/chat-command)

## 使用方法

### 基本命令

- `/开始猜选手` - 开始游戏（默认单局模式）
- `/猜选手 开始 [模式]` - 开始游戏，可选模式：Default, bo3, bo5
- `/猜选手 结束` - 结束当前游戏
- `/猜选手 排行榜` - 查看本周胜场排行榜
- `/猜选手 resetleaderboard` - 重置排行榜（管理员专用）



  _指令需要给予相关权限才可以使用_
- org.bcz.guesscs2proplayer.command.猜选手.开始
- org.bcz.guesscs2proplayer.command.猜选手.结束
- org.bcz.guesscs2proplayer.command.猜选手.排行榜
- org.bcz.guesscs2proplayer.command.猜选手.resetleaderboard
- org.bcz.guesscs2proplayer.command.* （省事但是不建议，可能被恶意删榜）
- org.bcz.guesscs2proplayer.command.开始猜选手

### 游戏规则

1. 游戏开始后，系统会随机选择一位 CS2 职业选手
2. 玩家通过发送选手名字进行猜测（如：s1mple）
3. 每次猜测后会显示对比表格，包含以下信息：
   - **选手姓名**：绿色表示正确，灰色表示错误
   - **队伍**：绿色表示正确，灰色表示错误
   - **国籍**：绿色表示正确，黄色表示同大洲，灰色表示错误
   - **年龄**：绿色表示正确，黄色表示相差≤2岁，灰色表示错误，箭头指示大小
   - **位置**：绿色表示正确，灰色表示错误
4. 每局有 10 次猜测机会
5. 猜对后根据游戏模式决定是否继续下一局

### 游戏模式

- **Default**：单局模式，猜对即获胜
- **BO3**：三局两胜制
- **BO5**：五局三胜制

## 数据管理

插件使用本地 CSV 文件存储选手数据，位于 `data/players.csv`。
选手数据使用本人项目https://github.com/BestBcz/prodown

### 插件截图

插件测试阶段 不代表最终品质

<img width="600" height="556" alt="b5606dd3de3a49fb3a098ce254c6fce6" src="https://github.com/user-attachments/assets/3cc557a3-7718-473c-935f-e6525169b115" />





### 支持的选手

插件支持大量 CS2 职业选手，包括但不限于：
- s1mple, ZywOo, NiKo, dev1ce
- coldzera, f0rest, GeT_RiGhT
- kennyS, olofmeister, GuardiaN
- 以及更多知名选手...

## 技术特性

- **现代化 UI**：采用 Windows 10 设计风格
- **高性能**：基于 Skia 图形库的图片生成
- **内存优化**：临时文件自动清理
- **错误处理**：完善的异常处理机制
- **并发安全**：支持多群同时游戏

## 文件结构




## 许可证

MIT License
