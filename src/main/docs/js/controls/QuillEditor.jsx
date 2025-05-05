import React, { useEffect, useRef, useState } from 'react';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

function QuillEditor({ initialContent = '', onChange, placeholder = 'Write something...' }) {
  const editorRef = useRef(null);
  const quillRef = useRef(null);
  const [content, setContent] = useState('');

  useEffect(() => {
    // Initialize Quill editor
    quillRef.current = new Quill(editorRef.current, {
      theme: 'snow',
      placeholder: placeholder,
      modules: {
        toolbar: [
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          ['bold', 'italic', 'underline', 'strike'],
          ['blockquote', 'code-block'],
          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'align': [] }],
          ['link', 'image'],
          ['clean']
        ]
      },
    });

    // Set initial content if provided
    if (initialContent) {
      quillRef.current.root.innerHTML = initialContent;
    }

    // Listen to content changes
    quillRef.current.on('text-change', () => {
      const html = quillRef.current.root.innerHTML;
      setContent(html);
      if (onChange) {
        onChange(html);
      }
    });

    // Cleanup on unmount
    return () => {
      if (quillRef.current) {
        quillRef.current = null;
      }
    };
  }, []); // Remove dependencies to prevent re-initialization

  // Update content when initialContent changes
  useEffect(() => {
    if (quillRef.current && initialContent) {
      quillRef.current.root.innerHTML = initialContent;
    }
  }, [initialContent]);

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
    </div>
  );
}

export default QuillEditor;