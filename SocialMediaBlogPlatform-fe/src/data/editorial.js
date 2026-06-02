import marbleDesign from '../assets/editorial/marble-design.png'
import minimalAi from '../assets/editorial/minimal-ai.png'
import officeCulture from '../assets/editorial/office-culture.png'
import slowCoffee from '../assets/editorial/slow-coffee.png'
import sarahJenkins from '../assets/editorial/sarah-jenkins.png'

export const categories = [
  { label: 'Design', slug: 'design', description: 'UI, UX, typography, visual systems' },
  { label: 'Culture', slug: 'culture', description: 'Society, media, books, internet culture' },
  { label: 'Technology', slug: 'technology', description: 'Software, AI, engineering, digital tools' },
  { label: 'Lifestyle', slug: 'lifestyle', description: 'Productivity, work, learning, wellness' },
]

export const authors = {
  sarah: {
    name: 'Sarah Jenkins',
    slug: 'sarah-jenkins',
    handle: '@sarahjenkins',
    title: 'Senior editor covering design and culture. Previously at The New York Times.',
    avatar: sarahJenkins,
  },
  marcus: {
    name: 'Marcus Chen',
    slug: 'marcus-chen',
    handle: '@marcuschen',
    title: 'Culture writer focused on cities, work, and everyday rituals.',
    avatar: sarahJenkins,
  },
}

export const articles = [
  {
    id: 'brutalist-web-design',
    title: 'The Renaissance of Brutalist Web Design',
    summary: 'Why designers are abandoning polished interfaces for raw, unstyled HTML and harsh typography in 2026.',
    category: 'Design',
    categorySlug: 'design',
    date: 'May 24, 2026',
    readTime: '6 min read',
    author: authors.sarah,
    image: marbleDesign,
  },
  {
    id: 'death-of-office',
    title: 'Digital Nomads and the Death of the Office',
    summary: 'As remote work becomes the permanent default, how are cities adapting to the exodus of tech workers?',
    category: 'Culture',
    categorySlug: 'culture',
    date: 'May 22, 2026',
    readTime: '8 min read',
    author: authors.marcus,
    image: officeCulture,
  },
  {
    id: 'minimalism-ai',
    title: 'Minimalism in the Age of AI',
    summary: 'When algorithms can generate infinite complexity, simplicity becomes the ultimate luxury.',
    category: 'Technology',
    categorySlug: 'technology',
    date: 'May 20, 2026',
    readTime: '5 min read',
    author: authors.sarah,
    image: minimalAi,
  },
  {
    id: 'slow-coffee',
    title: 'The Art of Slow Coffee',
    summary: 'Rediscovering the meditative process of manual brewing in a fast-paced world.',
    category: 'Lifestyle',
    categorySlug: 'lifestyle',
    date: 'May 18, 2026',
    readTime: '4 min read',
    author: authors.marcus,
    image: slowCoffee,
  },
  {
    id: 'typography-trends',
    title: 'Typography Trends for the Next Decade',
    summary: 'From variable fonts to kinetic typography, what will we be reading tomorrow?',
    category: 'Design',
    categorySlug: 'design',
    date: 'May 15, 2026',
    readTime: '7 min read',
    author: authors.sarah,
    image: slowCoffee,
  },
]
