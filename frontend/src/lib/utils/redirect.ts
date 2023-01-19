export function redirect (pageName: string){
    window.location.href = window.location.origin + `/${pageName}`;
}