# Sound_Recorder_Android
Android application simple sound recorder.

Android 4.4（API20） 

1. 项目采用MVC架构，录音、播放和Fragment采用Singleton设计模式
2. 所有界面通过Fragment实现，只有一个Activity
3. 可以实现录音文件的CRUD操作（录音文件存储在ExternalStoragePublicDirectory，录音前检验SD卡状态；重命名会进行验证重名空字符等错误）
4. 文件操作全部异步实现，不会出现ANR
5. 应用第一次运行时新建一个AsyncTask读取录音文件信息
6. 录音文件过多时，监听ListView的setOnScrollListener事件，缓存显示条目
7. 通过操作3个不同的ArrayList，实现录音记录的缓存、查找，查找非常流畅
8. 支持云存储，可以上传、下载、播放（Kii Cloud SDK）
9. Kii登录Token通过SharedPreferences方式存储
10. 后台录音时会在状态栏显示通知
11. 支持横竖屏切换UI，支持多语言
12. 无论切换横竖屏或点击Home Back键，避免出现内存泄露
13. 通过1000000次Monkey Test，持续时间近90分钟
14. 通过Trello管理整个项目，Github管理代码，Scrum开发过程
