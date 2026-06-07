import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight(str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // fallback
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

export function renderMarkdown(content: string): string {
  return md.render(content)
}

export default md
