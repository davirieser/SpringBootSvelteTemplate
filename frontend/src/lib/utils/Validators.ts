
export interface ValidatorResult {
    [validatorName: string]: {
        error: boolean;
        message?: string;
    }
}

export type ValidatorFn = (value: string) => ValidatorResult;


function required(value) {
    if (value === "" || value == null) {
        return { required: { error: true, message: 'Field is required' } };
    }
    return { required: { error: false } };
}

function minLength(number){
    return function (value){
        if(value == null || value.length < number){
            return {
                minLength : {
                    error: true,
                    value: number,
                    message: `Field must be at least ${number} characters long`
                },
            };
        }
        return { minLength: { error: false } };
    }
}

function maxLength(number){
    return function (value){
        if(value == null || value.length > number){
            return {
                maxLength : {
                    error: true,
                    value: number,
                    message: `Field must be at most ${number} characters long`
                },
            };
        }
        return { maxLength: { error: false } };
    }
}

function email(value){
    var mailForma = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    if(value == null || !mailForma.test(value)){
        return {
            email : {
                error: true,
                message: `Field must be a valid email address for example: test@gmail.com`
            },
        };
    }
    return { email: { error: false } };
}

export const Validators = {
    required,
    minLength,
    maxLength,
    email,
};
  

export function validateForm(e, form){
    const formData = new FormData(e.target);
    const data = {};
    for (let field of formData){
        const [key, value] = field;
        data[key] = value;
    }
    const errors = {};
    Object.keys(data).forEach((field)=> validateField(field, data[field], form, errors));
    return errors;
}

function validateField(field, value, form, errors){
    form[field]?.validators && 
        form[field].validators.forEach((fn)=>{
            const error = fn(value);
            errors[field] = {...errors[field], ...error};
        })
}

export function isFormValid(errors) {
    return Object.keys(errors).every((field) => { 
        return Object.keys(errors[field]).every((validator) => {
            return !errors[field][validator].error;
        });
    });
}




  