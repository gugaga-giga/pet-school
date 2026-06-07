<template>
  <div class="chat-message" :class="[`message-${message.role}`]">
    <div class="message-avatar">
      <el-avatar v-if="message.role === 'user'" :size="36">
        {{ userStore.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <el-avatar v-else :size="36" style="background: #409eff">
        <el-icon><ChatDotRound /></el-icon>
      </el-avatar>
    </div>
    <div class="message-body">
      <div class="message-header">
        <span class="message-name">{{ message.role === 'user' ? userStore.nickname : 'AI助手' }}</span>
        <span class="message-time">{{ formatRelativeTime(message.created_at) }}</span>
      </div>

      <!-- 用户消息 -->
      <div v-if="message.role === 'user'" class="message-content user-content">
        {{ message.content }}
      </div>

      <!-- AI消息 -->
      <div v-else class="message-content ai-content">
        <div v-if="message.content" class="markdown-body" v-html="renderedContent"></div>

        <!-- 意图标签 -->
        <div v-if="message.intent" class="message-intent">
          <IntentTag :intent="message.intent" />
        </div>

        <!-- 来源引用 -->
        <div v-if="message.sources && message.sources.length > 0" class="message-sources">
          <el-collapse>
            <el-collapse-item>
              <template #title>
                <span class="sources-title">
                  <el-icon><Link /></el-icon>
                  来源引用 ({{ message.sources.length }})
                </span>
              </template>
              <div
                v-for="(source, index) in message.sources"
                :key="index"
                class="source-item"
              >
                <div class="source-header">
                  <span class="source-name">{{ source.document_name }}</span>
                  <el-tag size="small" type="info">相关度: {{ (source.score * 100).toFixed(1) }}%</el-tag>
                </div>
                <p class="source-content">{{ source.content }}</p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 推荐产品 -->
        <div v-if="message.products && message.products.length > 0" class="message-products">
          <h4>推荐产品</h4>
          <div class="product-cards">
            <div v-for="product in message.products" :key="product.id" class="product-card">
              <div class="product-image">
                <el-image v-if="product.image" :src="product.image" fit="cover" />
                <el-icon v-else :size="32" color="#c0c4cc"><ShoppingBag /></el-icon>
              </div>
              <div class="product-info">
                <h5>{{ product.name }}</h5>
                <p>{{ product.description }}</p>
                <span class="product-price">¥{{ product.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'
import { formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import IntentTag from '@/components/IntentTag.vue'
import type { ChatMessage as ChatMessageType } from '@/types/api'

const props = defineProps<{
  message: ChatMessageType
}>()

const userStore = useUserStore()

const renderedContent = computed(() => {
  if (!props.message.content) return ''
  return renderMarkdown(props.message.content)
})
</script>

<style lang="scss" scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.message-user {
    flex-direction: row-reverse;

    .message-body {
      align-items: flex-end;
    }

    .message-header {
      flex-direction: row-reverse;
    }

    .user-content {
      background: var(--el-color-primary);
      color: #fff;
      border-radius: 12px 12px 2px 12px;
    }
  }

  .message-avatar {
    flex-shrink: 0;
  }

  .message-body {
    display: flex;
    flex-direction: column;
    max-width: 70%;

    .message-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .message-name {
        font-size: 13px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .message-time {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
      }
    }

    .message-content {
      padding: 12px 16px;
      border-radius: 12px 12px 12px 2px;
      font-size: 14px;
      line-height: 1.6;
    }

    .ai-content {
      background: var(--el-fill-color-light);
      border-radius: 12px 12px 12px 2px;
    }

    .message-intent {
      margin-top: 8px;
    }

    .message-sources {
      margin-top: 8px;

      .sources-title {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }

      .source-item {
        padding: 8px;
        border-radius: 6px;
        background: var(--el-fill-color-lighter);
        margin-bottom: 8px;

        .source-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;

          .source-name {
            font-size: 13px;
            font-weight: 600;
          }
        }

        .source-content {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          line-height: 1.5;
        }
      }
    }

    .message-products {
      margin-top: 12px;

      h4 {
        font-size: 14px;
        margin-bottom: 8px;
        color: var(--el-text-color-primary);
      }

      .product-cards {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 12px;

        .product-card {
          border: 1px solid var(--el-border-color-lighter);
          border-radius: 8px;
          overflow: hidden;
          cursor: pointer;
          transition: box-shadow 0.2s;

          &:hover {
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
          }

          .product-image {
            height: 100px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--el-fill-color-lighter);

            .el-image {
              width: 100%;
              height: 100%;
            }
          }

          .product-info {
            padding: 8px 12px;

            h5 {
              font-size: 14px;
              margin-bottom: 4px;
            }

            p {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .product-price {
              font-size: 16px;
              font-weight: 700;
              color: var(--el-color-danger);
            }
          }
        }
      }
    }
  }
}
</style>
