import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { $getSelection, $isRangeSelection, FORMAT_TEXT_COMMAND } from 'lexical';
import {
  $setBlocksType,
  $patchStyleText,
  $isAtNodeEnd
} from '@lexical/selection';
import {
  INSERT_ORDERED_LIST_COMMAND,
  INSERT_UNORDERED_LIST_COMMAND,
  REMOVE_LIST_COMMAND
} from '@lexical/list';
import { $createHeadingNode } from '@lexical/rich-text';

export function ToolbarPlugin() {
  const [editor] = useLexicalComposerContext();

  const formatBold = () => {
    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'bold');
  };

  const formatItalic = () => {
    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'italic');
  };

  const formatUnderline = () => {
    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'underline');
  };

  const formatHeading = (headingSize) => {
    editor.update(() => {
      const selection = $getSelection();
      if ($isRangeSelection(selection)) {
        $setBlocksType(selection, () => $createHeadingNode(headingSize));
      }
    });
  };

  const formatBulletList = () => {
    editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND);
  };

  const formatNumberedList = () => {
    editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND);
  };

  return (
    <div className="toolbar">
      <button
        onClick={formatBold}
        className="toolbar-item"
        aria-label="Format Bold"
      >
        Bold
      </button>
      <button
        onClick={formatItalic}
        className="toolbar-item"
        aria-label="Format Italics"
      >
        Italic
      </button>
      <button
        onClick={formatUnderline}
        className="toolbar-item"
        aria-label="Format Underline"
      >
        Underline
      </button>
      <button
        onClick={() => formatHeading('h1')}
        className="toolbar-item"
        aria-label="Format H1"
      >
        H1
      </button>
      <button
        onClick={() => formatHeading('h2')}
        className="toolbar-item"
        aria-label="Format H2"
      >
        H2
      </button>
      <button
        onClick={formatBulletList}
        className="toolbar-item"
        aria-label="Format Bullet List"
      >
        • Bullet List
      </button>
      <button
        onClick={formatNumberedList}
        className="toolbar-item"
        aria-label="Format Numbered List"
      >
        1. Numbered List
      </button>
    </div>
  );
}