import {Component, OnInit, Input, Output, EventEmitter, forwardRef} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
  selector: 'app-rating',
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css'],
  providers:[{provide:NG_VALUE_ACCESSOR,useExisting:forwardRef(()=>RatingComponent),multi:true}]
})
export class RatingComponent implements OnInit, ControlValueAccessor {
  @Input()
  get small(): boolean {
    return this._small;
  }

  set small(value: boolean) {
    this._small = value;
  }
  @Input()
  get fixed(): boolean {
    return this._fixed;
  }

  set fixed(value: boolean) {
    this._fixed = value;
  }
  onChange=(_)=>{};
  onTouched=()=>{};

  writeValue(obj: any): void {
    this.rating=obj;
    this.onChange(obj);
  }

  registerOnChange(fn: any): void {
    this.onChange=fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched=fn;
  }
  get onRatingSet(): EventEmitter<any> {
    return this._onRatingSet;
  }

  @Output()
  set onRatingSet(value: EventEmitter<any>) {
    this._onRatingSet = value;
  }
  @Input()
  get rating(): number {
    return this._rating;
  }

   private
  _small:boolean = false;

  private _fixed:boolean = false;

  set rating(value: number) {
    this._rating = value;
    this.onRatingSet.emit(this.rating);
    this.onChange(this._rating);
  }
  get numStars(): number {
    return this._numStars;
  }

  @Input()
  set numStars(value: number) {
    this._numStars = value;
  }
  get label(): string {
    return this._label;
  }

  @Input()
  set label(value: string) {
    this._label = value;
  }
  private _label:string;

  private _numStars:number;

  private _rating:number = 0;

  private _onRatingSet:EventEmitter<number> = new EventEmitter<number>();

  constructor() {
  }

  ngOnInit() {
    this.onRatingSet.emit(this.rating);
  }

  setRating(rating:number):void{
    this.rating = rating;
  }

  charForStar(index:number):string{
    return index<this.rating?"★":"☆";
  }

}
