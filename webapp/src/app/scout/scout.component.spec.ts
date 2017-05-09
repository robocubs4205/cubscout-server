import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScoutComponent } from './scout.component';

describe('ScoutComponent', () => {
  let component: ScoutComponent;
  let fixture: ComponentFixture<ScoutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
