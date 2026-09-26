import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import {CssBaseline, ThemeProvider} from '@mui/material';
import theme from './theme';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import './index.css';


ReactDOM.createRoot(document.getElementById('root')!).render(
    <ThemeProvider theme={theme} defaultMode="light">
        <CssBaseline/>
        <App/>
    </ThemeProvider>
);
