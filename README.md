# CS2 猜职业哥小游戏

一个基于 Mirai Console 的 CS2 职业选手猜猜猜游戏插件。

## 功能特性

- 🎮 **多种游戏模式**：支持单局、BO3、BO5 模式
- 🏆 **排行榜系统**：记录每周胜场排行榜
- 🎨 **现代化界面**：采用 Windows 10 风格设计的图片表格
- 🌍 **国旗显示**：支持国籍国旗显示
- 📊 **实时反馈**：颜色编码的猜测结果反馈

## 安装

1. 确保已安装 Mirai Console
2. 安装 mirai-skia-plugin 依赖
3. 将插件放入 plugins 文件夹
4. 重启 Mirai Console

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



![png](https://i.ibb.co/DD21bTFy/9fa4d7fde22239168f8a1c4d753a2655.png)



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
