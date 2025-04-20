import React, { useEffect, useState } from 'react';
import { LexicalComposer } from '@lexical/react/LexicalComposer';
import { RichTextPlugin } from '@lexical/react/LexicalRichTextPlugin';
import { ContentEditable } from '@lexical/react/LexicalContentEditable';
import { HistoryPlugin } from '@lexical/react/LexicalHistoryPlugin';
import { AutoFocusPlugin } from '@lexical/react/LexicalAutoFocusPlugin';
import { HeadingNode, QuoteNode } from '@lexical/rich-text';
import { TableCellNode, TableNode, TableRowNode } from '@lexical/table';
import { ListItemNode, ListNode } from '@lexical/list';
import { CodeHighlightNode, CodeNode } from '@lexical/code';
import { AutoLinkNode, LinkNode } from '@lexical/link';
import { LinkPlugin } from '@lexical/react/LexicalLinkPlugin';
import { ListPlugin } from '@lexical/react/LexicalListPlugin';
import { MarkdownShortcutPlugin } from '@lexical/react/LexicalMarkdownShortcutPlugin';
import { TRANSFORMERS } from '@lexical/markdown';
import { OnChangePlugin } from '@lexical/react/LexicalOnChangePlugin';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { $getRoot, $createParagraphNode, $createTextNode, $isRootOrShadowRoot } from 'lexical';
import { $generateHtmlFromNodes, $generateNodesFromDOM } from '@lexical/html';
import ToolbarPlugin from './editor/plugins/ToolbarPlugin';

function Placeholder() {
  return <div className="editor-placeholder">Enter your content here...</div>;
}

function HtmlToLexicalPlugin({ initialHtml }) {
  const [editor] = useLexicalComposerContext();

  useEffect(() => {
    if (initialHtml && initialHtml.trim()) {
      // Parse the HTML and update the editor
      editor.update(() => {
        const parser = new DOMParser();
        const dom = parser.parseFromString(initialHtml, 'text/html');
        const nodes = $generateNodesFromDOM(editor, dom);

        const root = $getRoot();
        root.clear();

        // If we have nodes from the HTML, add them
        if (nodes && nodes.length > 0) {
          nodes.forEach(node => {
            root.append(node);
          });
        } else {
          // Fallback if HTML parsing fails
          const paragraph = $createParagraphNode();
          paragraph.append($createTextNode(''));
          root.append(paragraph);
        }
      });
    }
  }, [initialHtml, editor]);

  return null;
}

const LexicalEditor = ({ initialValue, onChange }) => {
  const [editorState, setEditorState] = useState();

  // Initial editor configuration
  const editorConfig = {
    namespace: 'editor',
    theme: {
      root: 'editor-container',
      link: 'editor-link',
      text: {
        bold: 'editor-text-bold',
        italic: 'editor-text-italic',
        underline: 'editor-text-underline',
        strikethrough: 'editor-text-strikethrough',
        underlineStrikethrough: 'editor-text-underlineStrikethrough',
        code: 'editor-text-code'
      },
      heading: {
        h1: 'editor-heading-h1',
        h2: 'editor-heading-h2',
        h3: 'editor-heading-h3',
        h4: 'editor-heading-h4',
        h5: 'editor-heading-h5'
      },
      list: {
        ol: 'editor-list-ol',
        ul: 'editor-list-ul',
        listitem: 'editor-listitem'
      },
      quote: 'editor-quote',
      table: 'editor-table',
      tableCell: 'editor-tableCell',
      tableCellHeader: 'editor-tableCellHeader',
      tableRow: 'editor-tableRow'
    },
    onError(error) {
      console.error('Lexical Editor Error:', error);
    },
    nodes: [
      HeadingNode,
      ListNode,
      ListItemNode,
      QuoteNode,
      CodeNode,
      CodeHighlightNode,
      TableNode,
      TableCellNode,
      TableRowNode,
      AutoLinkNode,
      LinkNode
    ]
  };

  // HandleEditorChange will convert editor state to HTML and invoke the parent's onChange
  const handleEditorChange = (editorState) => {
    setEditorState(editorState);

    // Only trigger the onChange prop when we have editorState
    if (onChange) {
      editorState.read(() => {
        const htmlString = $generateHtmlFromNodes(editorState);
        onChange(htmlString);
      });
    }
  };

  return (
    <div className="editor-wrapper">
      <LexicalComposer initialConfig={editorConfig}>
        <div className="editor-container">
          <ToolbarPlugin />
          <div className="editor-inner">
            <RichTextPlugin
              contentEditable={<ContentEditable className="editor-input" />}
              placeholder={<Placeholder />}
            />
            <HistoryPlugin />
            <AutoFocusPlugin />
            <ListPlugin />
            <LinkPlugin />
            <MarkdownShortcutPlugin transformers={TRANSFORMERS} />
            {initialValue && <HtmlToLexicalPlugin initialHtml={initialValue} />}
            <OnChangePlugin onChange={handleEditorChange} />
          </div>
        </div>
      </LexicalComposer>
    </div>
  );
};

export default LexicalEditor;