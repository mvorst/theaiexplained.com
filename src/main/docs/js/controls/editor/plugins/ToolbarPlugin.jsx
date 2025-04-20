import React, { useCallback, useEffect, useState } from 'react';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import {
  $getSelection,
  $isRangeSelection,
  $createParagraphNode,
  $getNodeByKey,
  FORMAT_TEXT_COMMAND,
  FORMAT_ELEMENT_COMMAND,
  UNDO_COMMAND,
  REDO_COMMAND
} from 'lexical';
import {
  $isLinkNode,
  TOGGLE_LINK_COMMAND
} from '@lexical/link';
import {
  $isParentElementRTL,
  $isAtNodeEnd,
  $wrapNodes
} from '@lexical/selection';
import {
  $getNearestNodeOfType,
  $findMatchingParent,
  mergeRegister
} from '@lexical/utils';
import {
  INSERT_ORDERED_LIST_COMMAND,
  INSERT_UNORDERED_LIST_COMMAND,
  REMOVE_LIST_COMMAND,
  $isListNode,
  ListNode
} from '@lexical/list';
import {
  $createHeadingNode,
  $createQuoteNode,
  $isHeadingNode
} from '@lexical/rich-text';
import {
  $createCodeNode,
  $isCodeNode
} from '@lexical/code';

function getSelectedNode(selection) {
  const anchor = selection.anchor;
  const focus = selection.focus;
  const anchorNode = selection.anchor.getNode();
  const focusNode = selection.focus.getNode();
  if (anchorNode === focusNode) {
    return anchorNode;
  }
  const isBackward = selection.isBackward();
  if (isBackward) {
    return $isAtNodeEnd(focus) ? anchorNode : focusNode;
  } else {
    return $isAtNodeEnd(anchor) ? focusNode : anchorNode;
  }
}

export default function ToolbarPlugin() {
  const [editor] = useLexicalComposerContext();
  const [activeEditor, setActiveEditor] = useState(editor);
  const [blockType, setBlockType] = useState('paragraph');
  const [isBold, setIsBold] = useState(false);
  const [isItalic, setIsItalic] = useState(false);
  const [isUnderline, setIsUnderline] = useState(false);
  const [isStrikethrough, setIsStrikethrough] = useState(false);
  const [isCode, setIsCode] = useState(false);
  const [isLink, setIsLink] = useState(false);
  const [isRTL, setIsRTL] = useState(false);
  const [linkUrl, setLinkUrl] = useState('');
  const [showLinkInput, setShowLinkInput] = useState(false);

  const updateToolbar = useCallback(() => {
    const selection = $getSelection();
    if ($isRangeSelection(selection)) {
      const anchorNode = selection.anchor.getNode();
      const element =
        anchorNode.getKey() === 'root'
          ? anchorNode
          : anchorNode.getTopLevelElementOrThrow();
      const elementKey = element.getKey();
      const elementDOM = activeEditor.getElementByKey(elementKey);

      // Update text format
      setIsBold(selection.hasFormat('bold'));
      setIsItalic(selection.hasFormat('italic'));
      setIsUnderline(selection.hasFormat('underline'));
      setIsStrikethrough(selection.hasFormat('strikethrough'));
      setIsCode(selection.hasFormat('code'));
      setIsRTL($isParentElementRTL(selection));

      // Update links
      const node = getSelectedNode(selection);
      const parent = node.getParent();
      if ($isLinkNode(parent) || $isLinkNode(node)) {
        setIsLink(true);
      } else {
        setIsLink(false);
      }

      // Update block type
      if (elementDOM !== null) {
        if ($isHeadingNode(element)) {
          const tag = element.getTag();
          setBlockType(tag);
        } else if ($isListNode(element)) {
          const parentList = $getNearestNodeOfType(anchorNode, ListNode);
          const listType = parentList ? parentList.getListType() : element.getListType();
          setBlockType(listType === 'bullet' ? 'ul' : 'ol');
        } else if ($isCodeNode(element)) {
          setBlockType('code');
        } else if (element.getType() === 'quote') {
          setBlockType('quote');
        } else {
          setBlockType('paragraph');
        }
      }
    }
  }, [activeEditor]);

  useEffect(() => {
    return mergeRegister(
      editor.registerUpdateListener(({ editorState }) => {
        editorState.read(() => {
          updateToolbar();
        });
      }),
      editor.registerCommand(
        SELECTION_CHANGE_COMMAND,
        (_payload, newEditor) => {
          updateToolbar();
          setActiveEditor(newEditor);
          return false;
        },
        COMMAND_PRIORITY_LOW
      )
    );
  }, [editor, updateToolbar]);

  const formatParagraph = () => {
    if (blockType !== 'paragraph') {
      editor.update(() => {
        const selection = $getSelection();
        if ($isRangeSelection(selection)) {
          $wrapNodes(selection, () => $createParagraphNode());
        }
      });
    }
  };

  const formatHeading = (headingSize) => {
    if (blockType !== headingSize) {
      editor.update(() => {
        const selection = $getSelection();
        if ($isRangeSelection(selection)) {
          $wrapNodes(selection, () => $createHeadingNode(headingSize));
        }
      });
    }
  };

  const formatBulletList = () => {
    if (blockType !== 'ul') {
      editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND);
    } else {
      editor.dispatchCommand(REMOVE_LIST_COMMAND);
    }
  };

  const formatNumberedList = () => {
    if (blockType !== 'ol') {
      editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND);
    } else {
      editor.dispatchCommand(REMOVE_LIST_COMMAND);
    }
  };

  const formatQuote = () => {
    if (blockType !== 'quote') {
      editor.update(() => {
        const selection = $getSelection();
        if ($isRangeSelection(selection)) {
          $wrapNodes(selection, () => $createQuoteNode());
        }
      });
    }
  };

  const formatCode = () => {
    if (blockType !== 'code') {
      editor.update(() => {
        const selection = $getSelection();
        if ($isRangeSelection(selection)) {
          $wrapNodes(selection, () => $createCodeNode());
        }
      });
    }
  };

  const insertLink = () => {
    if (!isLink) {
      setShowLinkInput(true);
    } else {
      editor.dispatchCommand(TOGGLE_LINK_COMMAND, null);
    }
  };

  const confirmLink = () => {
    editor.dispatchCommand(TOGGLE_LINK_COMMAND, linkUrl);
    setShowLinkInput(false);
    setLinkUrl('');
  };

  return (
    <div className="toolbar">
      <button
        onClick={() => editor.dispatchCommand(UNDO_COMMAND)}
        className="toolbar-item"
        title="Undo"
      >
        <i className="format undo" />
      </button>
      <button
        onClick={() => editor.dispatchCommand(REDO_COMMAND)}
        className="toolbar-item"
        title="Redo"
      >
        <i className="format redo" />
      </button>
      <div className="divider" />
      <button
        onClick={formatParagraph}
        className={'toolbar-item ' + (blockType === 'paragraph' ? 'active' : '')}
        title="Paragraph"
      >
        <i className="format paragraph" />
      </button>
      <button
        onClick={() => formatHeading('h1')}
        className={'toolbar-item ' + (blockType === 'h1' ? 'active' : '')}
        title="Heading 1"
      >
        <i className="format h1" />
      </button>
      <button
        onClick={() => formatHeading('h2')}
        className={'toolbar-item ' + (blockType === 'h2' ? 'active' : '')}
        title="Heading 2"
      >
        <i className="format h2" />
      </button>
      <button
        onClick={() => formatHeading('h3')}
        className={'toolbar-item ' + (blockType === 'h3' ? 'active' : '')}
        title="Heading 3"
      >
        <i className="format h3" />
      </button>
      <button
        onClick={formatBulletList}
        className={'toolbar-item ' + (blockType === 'ul' ? 'active' : '')}
        title="Bullet List"
      >
        <i className="format ul" />
      </button>
      <button
        onClick={formatNumberedList}
        className={'toolbar-item ' + (blockType === 'ol' ? 'active' : '')}
        title="Numbered List"
      >
        <i className="format ol" />
      </button>
      <button
        onClick={formatQuote}
        className={'toolbar-item ' + (blockType === 'quote' ? 'active' : '')}
        title="Quote"
      >
        <i className="format quote" />
      </button>
      <button
        onClick={formatCode}
        className={'toolbar-item ' + (blockType === 'code' ? 'active' : '')}
        title="Code Block"
      >
        <i className="format code" />
      </button>
      <div className="divider" />
      <button
        onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'bold')}
        className={'toolbar-item ' + (isBold ? 'active' : '')}
        title="Bold"
      >
        <i className="format bold" />
      </button>
      <button
        onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'italic')}
        className={'toolbar-item ' + (isItalic ? 'active' : '')}
        title="Italic"
      >
        <i className="format italic" />
      </button>
      <button
        onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'underline')}
        className={'toolbar-item ' + (isUnderline ? 'active' : '')}
        title="Underline"
      >
        <i className="format underline" />
      </button>
      <button
        onClick={() => editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'strikethrough')}
        className={'toolbar-item ' + (isStrikethrough ? 'active' : '')}
        title="Strikethrough"
      >
        <i className="format strikethrough" />
      </button>
      <button
        onClick={insertLink}
        className={'toolbar-item ' + (isLink ? 'active' : '')}
        title="Link"
      >
        <i className="format link" />
      </button>
      {showLinkInput && (
        <div className="link-input">
          <input
            type="text"
            placeholder="Enter URL"
            value={linkUrl}
            onChange={(e) => setLinkUrl(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                confirmLink();
              }
            }}
          />
          <button onClick={confirmLink}>Confirm</button>
        </div>
      )}
    </div>
  );
}

// This variable is needed for the component to work properly
// It simulates the imported SELECTION_CHANGE_COMMAND from Lexical
const SELECTION_CHANGE_COMMAND = 'selection.change';
// And this simulates COMMAND_PRIORITY_LOW
const COMMAND_PRIORITY_LOW = 1;