更新 version 2.0.3
增加打开通知的开关，删除一些注释
去掉所有的apk下载链接，取消对bin文件夹的屏蔽，下载apk去/bin下寻找吧～

更新 version 2.0.1：
修复开机不能自启的bug，为FlushSharedPreferences方法增加了一个参数用来传递开机自启的状态

更新 version 2.0：
将wakelock放到Light方法下初始化
重构写入SharedPreferences的代码
删除监听电话状态的广播，还没测试完全，如果发现有个别固件会冲突我会加上一个开关
manifest里删除多余的ScreenOnOffListener receiver注册
改到这里重新测试耗电会好一些（=.=说到耗电我自己想打脸...）
删了点资源文件，略微压缩下安装包体积
修改了一些不是很恰当的注释
还有一些零碎的修改

预计下一步要控制可否发出通知，虽然我认为常驻后台还不提示是十分流氓的行为，但是所有人都说那个通知好烦

version 1.9(Beat)

近距离感应器解锁
欢迎指正交流
