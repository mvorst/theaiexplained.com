import React, { useEffect, useRef, useState } from 'react';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

function QuillEditor({ value = '', initialContent = '', onChange, placeholder = 'Write something...', readOnly = false }) {
  const editorRef = useRef(null);
  const quillRef = useRef(null);
  const [content, setContent] = useState('');

  useEffect(() => {
    // Initialize Quill editor
    quillRef.current = new Quill(editorRef.current, {
      theme: 'snow',
      placeholder: placeholder,
      readOnly: readOnly,
      modules: {
        toolbar: readOnly ? false : {
          container: [
            [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ 'list': 'ordered'}, { 'list': 'bullet' }],
            [{ 'color': [] }, { 'background': [] }],
            [{ 'align': [] }],
            ['link', 'image'],
            ['insertTable'],
            ['clean']
          ],
          handlers: {
            'insertTable': function() {
              insertTable();
            }
          }
        }
      },
    });

    // Custom table insertion function
    const insertTable = () => {
      const range = quillRef.current.getSelection();
      if (range) {
        // Create a simple 3x3 table HTML
        const tableHTML = `
          <table style="border-collapse: collapse; width: 100%; margin: 10px 0;">
            <tbody>
              <tr>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
                <td style="border: 1px solid #ccc; padding: 8px; min-width: 50px;">&nbsp;</td>
              </tr>
            </tbody>
          </table>
        `;
        quillRef.current.clipboard.dangerouslyPasteHTML(range.index, tableHTML);
      }
    };

    // Set initial content if provided
    const contentToSet = value || initialContent;
    if (contentToSet) {
      quillRef.current.root.innerHTML = contentToSet;
    }

    // Listen to content changes
    quillRef.current.on('text-change', () => {
      const html = quillRef.current.root.innerHTML;
      setContent(html);
      if (onChange) {
        onChange(html);
      }
    });

    // Add table editing capabilities
    const addTableEditingFeatures = () => {
      const editor = quillRef.current.root;
      
      // Add context menu for table cells
      editor.addEventListener('contextmenu', (e) => {
        const target = e.target.closest('td, th');
        if (target) {
          e.preventDefault();
          showTableContextMenu(e, target);
        }
      });

      // Add keyboard shortcuts for table navigation
      editor.addEventListener('keydown', (e) => {
        const target = e.target.closest('td, th');
        if (target) {
          if (e.key === 'Tab') {
            e.preventDefault();
            navigateTableCell(target, e.shiftKey ? 'prev' : 'next');
          }
        }
      });
    };

    const showTableContextMenu = (e, cell) => {
      // Remove existing context menu
      const existingMenu = document.querySelector('.table-context-menu');
      if (existingMenu) {
        existingMenu.remove();
      }

      const menu = document.createElement('div');
      menu.className = 'table-context-menu';
      menu.style.cssText = `
        position: fixed;
        top: ${e.clientY}px;
        left: ${e.clientX}px;
        background: white;
        border: 1px solid #ccc;
        border-radius: 4px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        z-index: 1000;
        padding: 5px 0;
        min-width: 150px;
      `;

      const menuItems = [
        { text: 'Insert Row Above', action: () => insertRowAbove(cell) },
        { text: 'Insert Row Below', action: () => insertRowBelow(cell) },
        { text: 'Insert Column Left', action: () => insertColumnLeft(cell) },
        { text: 'Insert Column Right', action: () => insertColumnRight(cell) },
        { text: 'Delete Row', action: () => deleteRow(cell) },
        { text: 'Delete Column', action: () => deleteColumn(cell) },
        { text: 'Delete Table', action: () => deleteTable(cell) }
      ];

      menuItems.forEach(item => {
        const menuItem = document.createElement('div');
        menuItem.textContent = item.text;
        menuItem.style.cssText = `
          padding: 8px 15px;
          cursor: pointer;
          font-size: 14px;
        `;
        menuItem.addEventListener('mouseenter', () => {
          menuItem.style.backgroundColor = '#f0f0f0';
        });
        menuItem.addEventListener('mouseleave', () => {
          menuItem.style.backgroundColor = '';
        });
        menuItem.addEventListener('click', () => {
          item.action();
          menu.remove();
        });
        menu.appendChild(menuItem);
      });

      document.body.appendChild(menu);

      // Remove menu when clicking outside
      setTimeout(() => {
        document.addEventListener('click', () => menu.remove(), { once: true });
      }, 0);
    };

    const navigateTableCell = (currentCell, direction) => {
      const cells = Array.from(currentCell.closest('table').querySelectorAll('td, th'));
      const currentIndex = cells.indexOf(currentCell);
      let targetIndex;

      if (direction === 'next') {
        targetIndex = currentIndex + 1;
        if (targetIndex >= cells.length) targetIndex = 0;
      } else {
        targetIndex = currentIndex - 1;
        if (targetIndex < 0) targetIndex = cells.length - 1;
      }

      if (cells[targetIndex]) {
        cells[targetIndex].focus();
        const range = document.createRange();
        range.selectNodeContents(cells[targetIndex]);
        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
      }
    };

    const insertRowAbove = (cell) => {
      const row = cell.closest('tr');
      const newRow = row.cloneNode(true);
      newRow.querySelectorAll('td, th').forEach(cell => cell.innerHTML = '&nbsp;');
      row.parentNode.insertBefore(newRow, row);
      quillRef.current.update();
    };

    const insertRowBelow = (cell) => {
      const row = cell.closest('tr');
      const newRow = row.cloneNode(true);
      newRow.querySelectorAll('td, th').forEach(cell => cell.innerHTML = '&nbsp;');
      row.parentNode.insertBefore(newRow, row.nextSibling);
      quillRef.current.update();
    };

    const insertColumnLeft = (cell) => {
      const table = cell.closest('table');
      const cellIndex = Array.from(cell.parentNode.children).indexOf(cell);
      const rows = table.querySelectorAll('tr');
      
      rows.forEach(row => {
        const newCell = document.createElement(row.querySelector('th') ? 'th' : 'td');
        newCell.innerHTML = '&nbsp;';
        newCell.style.cssText = cell.style.cssText;
        row.insertBefore(newCell, row.children[cellIndex]);
      });
      quillRef.current.update();
    };

    const insertColumnRight = (cell) => {
      const table = cell.closest('table');
      const cellIndex = Array.from(cell.parentNode.children).indexOf(cell);
      const rows = table.querySelectorAll('tr');
      
      rows.forEach(row => {
        const newCell = document.createElement(row.querySelector('th') ? 'th' : 'td');
        newCell.innerHTML = '&nbsp;';
        newCell.style.cssText = cell.style.cssText;
        if (cellIndex + 1 < row.children.length) {
          row.insertBefore(newCell, row.children[cellIndex + 1]);
        } else {
          row.appendChild(newCell);
        }
      });
      quillRef.current.update();
    };

    const deleteRow = (cell) => {
      const row = cell.closest('tr');
      const table = row.closest('table');
      if (table.querySelectorAll('tr').length > 1) {
        row.remove();
        quillRef.current.update();
      }
    };

    const deleteColumn = (cell) => {
      const table = cell.closest('table');
      const cellIndex = Array.from(cell.parentNode.children).indexOf(cell);
      const rows = table.querySelectorAll('tr');
      
      if (rows[0].children.length > 1) {
        rows.forEach(row => {
          if (row.children[cellIndex]) {
            row.children[cellIndex].remove();
          }
        });
        quillRef.current.update();
      }
    };

    const deleteTable = (cell) => {
      const table = cell.closest('table');
      table.remove();
      quillRef.current.update();
    };

    addTableEditingFeatures();

    // Cleanup on unmount
    return () => {
      if (quillRef.current) {
        quillRef.current = null;
      }
    };
  }, []); // Remove dependencies to prevent re-initialization

  // Update content when value or initialContent changes
  useEffect(() => {
    if (quillRef.current) {
      const contentToSet = value || initialContent;
      const currentContent = quillRef.current.root.innerHTML;
      // Only update if content has actually changed to avoid cursor issues
      if (contentToSet && contentToSet !== currentContent) {
        quillRef.current.root.innerHTML = contentToSet;
      }
    }
  }, [value, initialContent]);

  // Function to get HTML content
  const getHTML = () => {
    return quillRef.current.root.innerHTML;
  };

  // Function to set content
  const setEditorContent = (htmlContent) => {
    if (quillRef.current) {
      quillRef.current.root.innerHTML = htmlContent;
    }
  };

  return (
    <div className="quill-editor-container">
      <div ref={editorRef} className="editor" />
      <style jsx>{`
        .quill-editor-container table {
          border-collapse: collapse;
          width: 100%;
          margin: 10px 0;
        }
        
        .quill-editor-container table td,
        .quill-editor-container table th {
          border: 1px solid #ccc;
          padding: 8px;
          min-width: 50px;
          vertical-align: top;
          position: relative;
        }
        
        .quill-editor-container table td:hover,
        .quill-editor-container table th:hover {
          background-color: #f8f9fa;
        }
        
        .quill-editor-container table td:focus,
        .quill-editor-container table th:focus {
          outline: 2px solid #007bff;
          background-color: #e3f2fd;
        }
        
        .table-context-menu {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        
        .table-context-menu div:hover {
          background-color: #f0f0f0 !important;
        }
      `}</style>
    </div>
  );
}

export default QuillEditor;