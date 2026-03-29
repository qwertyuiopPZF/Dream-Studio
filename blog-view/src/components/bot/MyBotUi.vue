<template>
  <div class="bot-container">
    <div class="messages-area" ref="scrollRef">
      <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
        <div class="bubble">
          <span
            v-if="msg.role === 'bot' && msg.type !== 'rating' && msg.text"
            class="typer-target"
            :ref="(el) => initTypeEffect(el, msg, index)"
          ></span>

          <div v-else-if="msg.type === 'rating'" class="rating-container">
            <span class="rating-label">请打分：</span>
            <el-rate
              v-model="msg.ratingValue"
              :disabled="msg.rated"
              @change="(val) => handleRating(val, msg)"
              size="large"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
          </div>
          <span v-else>{{ msg.text }}</span>
        </div>
      </div>

      <div class="action-area" :class="{ show: !isTyping && currentOptions.length }">
        <button
          v-for="(opt, idx) in currentOptions"
          :key="idx"
          class="action-btn"
          @click="handleUserSelect(opt)"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import Typed from 'typed.js'
// text: 支持 HTML 标签，支持 ^500 暂停语法
// next: 下一步的 key
// options: 用户选项
const script = {
  // 第一句
  root: {
    text: '你好，这里是 Dream-studio 👋 ^500',
    next: 'intro_1',
    options: [],
  },

  intro_1: {
    text: '梦工厂的博客论坛 😘 ^500', // ^500 稍微停顿一下
    next: 'intro_2',
    options: [],
  },

  intro_2: {
    text: '本博客论坛致力于为同学们提供纯净、积极的交流空间，鼓励大家记录思考、分享成果、互帮互助、共同成长。💪^500',
    // 这里给选项，等待用户点击
    options: [{ label: '然后呢？😄', next: 'story' }],
  },

  story: {
    text: '😘',
    next: 'story_2',
    options: [],
  },
  story_2: {
    text: '无论是学习感悟、课程笔记、原创文章，还是遇到的困惑与难题，都可以在这里分享与交流。 ^500',
    next: 'story_3',
    options: [{ label: '关于本项目 🤔', next: 'story_3' }],
  },
  story_3: {
    text: '如大家对此项目感兴趣，右上角传送门 ^500',
    next: 'story_4',
    options: [],
  },
  story_4: {
    text: '欢迎大家拉取源码 ^500',
    next: 'story_5',
    options: [],
  },
  story_5: {
    text: '愿每一位同学都能在这里收获知识、收获成长，一起进步，一起优秀 ^500',
    next: 'story_6',
    options: [],
  },

  story_6: {
    text: '本项目是学习中的产物，如有不足还请见谅，如有建议，欢迎提出！😀我们会加油努力的！ ^500',
    next: 'story_7',
    options: [],
  },
  story_7: {
    text: '给 Dream-studio 评个星吧 ^500',
    next: 'story_8',
    options: [],
  },
  story_8: {
    // 标记这是一条特殊消息：评分
    type: 'rating',
    next: 'story_9',
  },
  story_9: {
    text: '收到！谢谢你的鼓励 😘 ^500',
    options: [{ label: '再见' }],
  },
}

const messages = ref([]) // 消息历史
const currentOptions = ref([]) // 当前显示的按钮
const isTyping = ref(false) // 是否正在打字
const scrollRef = ref(null) // 滚动容器

const botSay = async (nodeKey) => {
  const node = script[nodeKey]
  if (!node) return

  isTyping.value = true
  currentOptions.value = [] // 隐藏按钮

  // 推入一条新消息，但内容暂时为空，text 存主要内容供 Typed 使用
  messages.value.push({
    role: 'bot',
    text: node.text || '',
    type: node.type || 'text',
    options: node.options, // 存下来，等打完字再显示
    nextNode: node.next, // 存下来，如果没有选项，就自动跳转
    typedInstance: null, // 存 Typed 实例，方便销毁
  })

  await scrollToBottom()
  if (node.type === 'rating') {
    isTyping.value = false
  }
}
const initTypeEffect = (el, msg, index) => {
  if (!el || el.innerHTML || index !== messages.value.length - 1) return

  const typed = new Typed(el, {
    strings: [msg.text],
    typeSpeed: 50,
    startDelay: 300,
    showCursor: true, // 显示光标
    cursorChar: '_', // 光标样式
    contentType: 'html', // 允许 text 中包含 <br> 等 html 标签
    onStringTyped: () => {
      scrollToBottom()
    },
    onComplete: () => {
      // 打字完成
      msg.typedInstance = typed // 保存实例
      if (typed.cursor) {
        typed.cursor.remove()
      }

      isTyping.value = false

      if (msg.options && msg.options.length > 0) {
        // A. 如果有选项，显示选项
        currentOptions.value = msg.options
        scrollToBottom()
      } else if (msg.nextNode) {
        // B. 如果没有选项但有 nextNode，说明是连续对话，自动触发下一句
        botSay(msg.nextNode)
      }
    },
  })
}

const handleUserSelect = (opt) => {
  messages.value.push({ role: 'user', text: opt.label })
  currentOptions.value = []
  scrollToBottom()

  // 2. 触发下一句
  if (opt.next) {
    // 稍微延迟一下，感觉更自然
    setTimeout(() => {
      botSay(opt.next)
    }, 1000)
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  }
}

onMounted(() => {
  botSay('root')
})

const handleRating = (score, msg) => {
  msg.rated = true

  messages.value.push({
    role: 'user',
    text: `${score} 星好评！`,
  })

  scrollToBottom()

  // 2. 触发下一步
  if (msg.nextNode) {
    setTimeout(() => {
      botSay(msg.nextNode)
    }, 600)
  }
}
</script>

<style scoped>
.bot-container {
  max-width: 700px;
  min-height: 500px;
  max-height: 900px;
  margin: 30px auto;
  /* 背景改为深灰色 */
  background: rgba(249, 249, 249, 0.8);
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  border-radius: 15px;

  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #fff;
}

/* 顶部栏透明化 */
.header {
  background: rgba(0, 0, 0, 0.2);
  border-bottom: none;
  padding: 15px;
}
.title {
  color: rgba(255, 255, 255, 0.8);
}

/* 消息区域 */
.messages-area {
  flex: 1;
  padding: 20px 30px;
  overflow-y: auto;
  scrollbar-width: none;
}

.messages-area::-webkit-scrollbar {
  display: none;
}

.message-row {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.message-row.user {
  flex-direction: row-reverse;
}

.bubble {
  padding: 10px 18px;
  border-radius: 25px;
  font-size: 13px;
  line-height: 1.5;
  max-width: 80%;
  position: relative;
  letter-spacing: 0.5px;
}

.bot .bubble {
  background: linear-gradient(135deg, #0eb595);
  color: #fff;
  border-bottom-left-radius: 5px;
  margin-right: auto;
}

.user .bubble {
  background: linear-gradient(135deg, #16816e);
  color: #fff;
  border-bottom-right-radius: 5px;
  margin-left: auto;
}

.action-area {
  padding: 10px 0 30px;
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

.action-btn {
  background: linear-gradient(135deg, #16816e);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
}

.action-btn:hover {
  background: #1dccaa;
  border-color: #1dccaa;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(29, 204, 170, 0.4);
}
.rating-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5px;
}

.rating-label {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 5px;
}

.stars {
  display: flex;
  gap: 8px;
  font-size: 18px; /* 星星大小 */
  cursor: pointer;
}

.star-icon {
  transition: transform 0.2s;
}
</style>
