import { Extension } from '@tiptap/core'
import { Plugin, PluginKey } from 'prosemirror-state'

export interface PasteImageOptions {
  onImagePaste: (file: File) => void
}

export const PasteImage = Extension.create<PasteImageOptions>({
  name: 'pasteImage',

  addOptions() {
    return {
      onImagePaste: () => {},
    }
  },

  addProseMirrorPlugins() {
    const { onImagePaste } = this.options
    return [
      new Plugin({
        key: new PluginKey('pasteImage'),
        props: {
          handlePaste: (view, event) => {
            const items = event.clipboardData?.items
            if (!items) return false

            for (const item of items) {
              if (item.type.startsWith('image/')) {
                event.preventDefault()
                const blob = item.getAsFile()
                if (blob) {
                  onImagePaste(blob)
                }
                return true
              }
            }
            return false
          },
        },
      }),
    ]
  },
})
