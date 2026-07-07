import React, { useState, useEffect } from 'react';
import './CommentClapButton.css';

export function CommentClapButton({ comment, onClap, currentUserId }) {
  const initialClaps = comment.stats?.clapCount || 0;
  const isClapped = comment.clappedByCurrentUser || false;
  
  const storageKey = `clap_${comment.id}_${currentUserId || 'guest'}`;

  const [sessionClaps, setSessionClaps] = useState(() => {
    if (isClapped) {
      const stored = localStorage.getItem(storageKey);
      return stored ? parseInt(stored, 10) : 1;
    }
    return 0;
  });
  const [showBubble, setShowBubble] = useState(false);

  // Sync state if it was un-clapped (e.g. undo)
  useEffect(() => {
    if (!isClapped) {
      setSessionClaps(0);
      localStorage.removeItem(storageKey);
    }
  }, [isClapped, storageKey]);

  const handleClap = (e) => {
    e.preventDefault();
    e.stopPropagation();

    setSessionClaps(prev => {
      const newVal = prev + 1;
      localStorage.setItem(storageKey, newVal.toString());
      return newVal;
    });
    setShowBubble(true);
    
    onClap(comment, false);
  };

  useEffect(() => {
    if (showBubble) {
      const timer = setTimeout(() => {
        setShowBubble(false);
      }, 1500); 
      return () => clearTimeout(timer);
    }
  }, [sessionClaps, showBubble]);

  const displayCount = new Intl.NumberFormat('en-US', { 
    notation: "compact", 
    compactDisplay: "short", 
    maximumFractionDigits: 1 
  }).format(initialClaps);

  return (
    <div className="comment-clap-wrapper">
      <div className={`clap-bubble ${showBubble ? 'show' : ''}`}>
        +{sessionClaps}
      </div>
      <button 
        className={`comment-clap-btn ${isClapped ? 'clapped' : ''}`}
        onClick={handleClap}
        type="button"
        title="Clap"
      >
        {isClapped ? (
          <svg 
            viewBox="0 0 512 512" 
            width="24" height="24"
            fill="currentColor"
          >
            <path d="M336 16v64c0 8.8-7.2 16-16 16s-16-7.2-16-16V16c0-8.8 7.2-16 16-16s16 7.2 16 16m-98.7 7.1l32 48c4.9 7.4 2.9 17.3-4.4 22.2s-17.3 2.9-22.2-4.4l-32-48c-4.9-7.4-2.9-17.3 4.4-22.2s17.3-2.9 22.2 4.4M135 119c9.4-9.4 24.6-9.4 33.9 0l123.8 123.7c10.1 10.1 27.3 2.9 27.3-11.3V192c0-17.7 14.3-32 32-32s32 14.3 32 32v153.6c0 57.1-30 110-78.9 139.4c-64 38.4-145.8 28.3-198.5-24.4L7 361c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l53 53c6.1 6.1 16 6.1 22.1 0s6.1-16 0-22.1l-93-93c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l93 93c6.1 6.1 16 6.1 22.1 0s6.1-16 0-22.1L55 185c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l117 117c6.1 6.1 16 6.1 22.1 0s6.1-16 0-22.1l-93-93c-9.4-9.4-9.4-24.6 0-33.9zm298.1 365.9c-24.2 14.5-50.9 22.1-77.7 23.1c48.1-39.6 76.6-99 76.6-162.4v-98.1c8.2-.1 16-6.4 16-16v-39.4c0-17.7 14.3-32 32-32s32 14.3 32 32v153.6c0 57.1-30 110-78.9 139.4zm-8.2-466.2c7.4 4.9 9.3 14.8 4.4 22.2l-32 48c-4.9 7.4-14.8 9.3-22.2 4.4s-9.3-14.8-4.4-22.2l32-48c4.9-7.4 14.8-9.3 22.2-4.4" />
          </svg>
        ) : (
          <svg 
            viewBox="0 0 256 256" 
            width="24" height="24"
            fill="currentColor"
          >
            <path d="M160.22 24V8a8 8 0 0 1 16 0v16a8 8 0 0 1-16 0m35.88 17a7.9 7.9 0 0 0 4.17 1.17a8 8 0 0 0 6.84-3.83l8-13.11a8 8 0 0 0-13.68-8.33l-8 13.1a8 8 0 0 0 2.67 11m47.51 12.59a8 8 0 0 0-10.08-5.16l-15.06 4.85a8 8 0 0 0 2.46 15.62a8.2 8.2 0 0 0 2.46-.39l15.05-4.85a8 8 0 0 0 5.17-10.11ZM217 97.58a80.22 80.22 0 0 1-10.22 94c-.34 1.73-.72 3.46-1.19 5.18A80.17 80.17 0 0 1 58.77 216L23.5 155a26 26 0 0 1 19.24-38.79l-3-5.2a26 26 0 0 1 19.2-38.78l-.7-1.23a26 26 0 0 1 37.23-34.47a26.06 26.06 0 0 1 44.83.47l12.26 21.2a26.07 26.07 0 0 1 43.25 2.8ZM109.07 55l25 43.17a26 26 0 0 1 17.33-10L126.42 45a10 10 0 1 0-17.35 10m-36.95 8l6.46 11.17a26.05 26.05 0 0 1 17.32-10L89.45 53a10 10 0 1 0-17.33 10m111.54 81l-20.22-35a10 10 0 0 0-17.74 9.25L158.3 140a8 8 0 0 1-13.87 8l-36.5-63a10 10 0 1 0-17.35 10l26.05 45a8 8 0 0 1-13.87 8L71 93a10 10 0 0 0-17.33 10l35.22 61A8 8 0 0 1 75 172l-20.28-35a10 10 0 0 0-17.34 10l35.27 61a64.12 64.12 0 0 0 117.42-15.44a63.52 63.52 0 0 0-6.41-48.56m19.41-38.42L181.93 69a10 10 0 0 0-17.38 10l33 57.05a80.2 80.2 0 0 1 9.45 25.46a64.23 64.23 0 0 0-3.93-55.93" />
          </svg>
        )}
      </button>
      <span className="comment-clap-count">{initialClaps < 1000 ? initialClaps : displayCount}</span>
    </div>
  );
}
