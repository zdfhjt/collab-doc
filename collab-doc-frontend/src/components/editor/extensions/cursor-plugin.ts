import { Extension } from '@tiptap/core'
import { Plugin, PluginKey } from 'prosemirror-state'
import type { WebsocketProvider } from 'y-websocket'

export interface CursorPluginOptions {
  provider: WebsocketProvider
}

export const CursorPlugin = Extension.create<CursorPluginOptions>({
  name: 'cursorPlugin',

  addProseMirrorPlugins() {
    const provider = this.options.provider
    return [
      new Plugin({
        key: new PluginKey('cursorPlugin'),
        props: {
          decorations: () => null,
        },
        view: () => ({
          update: (view) => {
            const { anchor } = view.state.selection
            provider.awareness.setLocalStateField('cursor', {
              anchor,
              head: view.state.selection.head,
            })
          },
        }),
      }),
    ]
  },
})
