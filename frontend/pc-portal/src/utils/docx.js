import { Document, Packer, Paragraph, TextRun, AlignmentType } from 'docx'

/**
 * 将文书内容（纯文本）生成规范的 .docx Blob。
 * 标题居中加粗，正文宋体、首行缩进，短行末尾带冒号的视为小节标题加粗。
 */
export async function buildDocx(title, content) {
  const lines = (content || '').split('\n')
  const children = []

  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 400 },
    children: [new TextRun({ text: title, bold: true, size: 32, font: '宋体' })],
  }))

  for (const line of lines) {
    const t = line.trim()
    if (!t) {
      children.push(new Paragraph({ children: [] }))
      continue
    }
    const isHeading = /^[^\s]{1,12}[：:]$/.test(t)
    children.push(new Paragraph({
      spacing: { after: 120, line: 360 },
      indent: isHeading ? undefined : { firstLine: 480 },
      children: [new TextRun({ text: t, size: 24, font: '宋体', bold: isHeading })],
    }))
  }

  return Packer.toBlob(new Document({ sections: [{ children }] }))
}

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
