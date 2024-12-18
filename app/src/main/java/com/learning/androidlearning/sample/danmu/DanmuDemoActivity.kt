package com.learning.androidlearning.sample.danmu

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.learning.androidlearning.R
import android.widget.Button
import android.util.Log

// Base64 编码的 URL: "aHR0cHM6Ly9hbGlpbWcuY2hhbmdiYS5jb20vY2FjaGUvcGhvdG8vOTc2NTMyODc5XzIwMF8yMDAuanBn"
private val url = "aHR0cHM6Ly9hbGlpbWcuY2hhbmdiYS5jb20vY2FjaGUvcGhvdG8vOTc2NTMyODc5XzIwMF8yMDAuanBn"

class DanmuDemoActivity : AppCompatActivity() {
    private lateinit var danmuView: DanmuView
    private lateinit var danmuInput: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var replayButton: Button

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            String(Base64.decode(base64Url, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e("DanmuDemo", "Base64 decode failed", e)
            "" // 解码失败返回空字符串
        }
    }

    private val testDanmuList = listOf(
        // 第一列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "短名字",
            content = "这是一条很短的弹幕",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "这是一个特别长的用户名称",
            content = "中等长度的弹幕内容示例",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "普通用户",
            content = "这是一条非常非常非常非常非常非常非常非常长的弹幕内容，测试长文本的显示效果",
            image = "ic_red_packet"
        ),
        // 第二列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户A",
            content = "第二列第一条",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户B很长很长",
            content = "第二列第二条弹幕",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户C",
            content = "第二列第三条超长超长超长超长超长的弹幕",
            image = "ic_red_packet"
        ),
        // 第三列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "游客1号",
            content = "这是第三列的弹幕",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "热心网友",
            content = "弹幕测试继续进行中...",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "匿名用户",
            content = "第三列结束",
            image = "ic_red_packet"
        ),
        // 第四列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "程序员",
            content = "Debug中...",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "测试工程师",
            content = "这是一条测试弹幕，请勿回复",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "产品经理",
            content = "这个功能需要优化一下",
            image = "ic_red_packet"
        ),
        // 第五列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "设计师",
            content = "UI还需要调整",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "运营小姐姐",
            content = "欢迎大家踊跃发言~",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "管理员",
            content = "请文明发言，禁止刷屏",
            image = "ic_red_packet"
        ),
        // 第六列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "路人甲",
            content = "我就是来看看",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "吃瓜群众",
            content = "前排围观，带好瓜子",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "围观群众",
            content = "这个弹幕效果不错哦",
            image = "ic_red_packet"
        ),
        // 第七列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "技术支持",
            content = "系统运行正常",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "安全工程师",
            content = "请注意网络安全",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "维护人员",
            content = "定期维护中...",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "数据分析师",
            content = "数据分析中...",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "前端开发",
            content = "UI优化完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "后端工程师",
            content = "接口开发中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "实习生小王",
            content = "学习ing",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "产品助理",
            content = "需求整理完毕",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "运维工程师",
            content = "服务器维护中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "测试助理",
            content = "bug修复验证",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "UI设计师",
            content = "界面设计优化",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "产品实习生",
            content = "原型设计中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "开发leader",
            content = "代码审核完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "项目经理",
            content = "项目进度正常",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "运营实习生",
            content = "数据统计中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "客服小李",
            content = "用户反馈处理中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "市场专员",
            content = "活动策划完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "人事助理",
            content = "面试安排中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "财务专员",
            content = "报销处理中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "行政助理",
            content = "会议室预订完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "实习生小张",
            content = "文档整理中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "运营经理",
            content = "数据分析报告完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "技术总监",
            content = "架构评审通过",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "产品总监",
            content = "产品规划讨论中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "测试经理",
            content = "测试计划审核完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "运维总监",
            content = "系统升级计划确认",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "设计总监",
            content = "设计方案通过",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "市场总监",
            content = "营销策略确定",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "人力总监",
            content = "人才计划制定完成",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "财务总监",
            content = "预算审核通过",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "CEO",
            content = "战略规划讨论中",
            image = "ic_red_packet"
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "CTO",
            content = "技术路线确定",
            image = "ic_red_packet"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danmu_demo)

        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)
        replayButton = findViewById(R.id.replayButton)

        // 设置测试数据
        danmuView.setDanmuList(testDanmuList)

        // 发送按钮点击事件
        sendButton.setOnClickListener {
            val content = danmuInput.text?.toString()
            if (!content.isNullOrEmpty()) {
                val newDanmu = DanmuItem(
                    avatar = decodeBase64Url(url), // 使用解码后的 URL
                    username = "用户名",
                    content = content,
                    image = "ic_red_packet"
                )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        // 重播按钮点击事件
        replayButton.setOnClickListener {
            danmuView.replay()
        }

        // 弹幕完成回调
        danmuView.setOnDanmuCompleteListener { danmu ->
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }
    }
} 